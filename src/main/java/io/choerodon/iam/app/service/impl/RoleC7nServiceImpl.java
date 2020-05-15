package io.choerodon.iam.app.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.iam.api.vo.RoleNameAndEnabledVO;
import io.choerodon.iam.api.vo.UserRoleVO;
import io.choerodon.iam.api.vo.agile.RoleUserCountVO;
import io.choerodon.iam.app.service.RoleC7nService;
import io.choerodon.iam.infra.constant.LabelC7nConstants;
import io.choerodon.iam.infra.dto.ProjectDTO;
import io.choerodon.iam.infra.dto.RoleAssignmentSearchDTO;
import io.choerodon.iam.infra.dto.RoleC7nDTO;
import io.choerodon.iam.infra.mapper.ProjectMapper;
import io.choerodon.iam.infra.mapper.ProjectUserMapper;
import io.choerodon.iam.infra.mapper.RoleC7nMapper;
import io.choerodon.iam.infra.utils.ConvertUtils;
import io.choerodon.iam.infra.utils.PageUtils;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.iam.api.dto.RoleDTO;
import org.hzero.iam.domain.entity.Role;
import org.hzero.iam.domain.vo.RoleVO;
import org.hzero.iam.infra.mapper.RoleMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/5/12 17:00
 */
@Service
public class RoleC7nServiceImpl implements RoleC7nService {

    private RoleC7nMapper roleC7nMapper;
    private ProjectUserMapper projectUserMapper;
    private ProjectMapper projectMapper;
    private RoleMapper roleMapper;

    public RoleC7nServiceImpl(RoleC7nMapper roleC7nMapper, ProjectUserMapper projectUserMapper, ProjectMapper projectMapper, RoleMapper roleMapper) {
        this.roleC7nMapper = roleC7nMapper;
        this.projectUserMapper = projectUserMapper;
        this.projectMapper = projectMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public List<io.choerodon.iam.api.vo.agile.RoleVO> listRolesWithUserCountOnProjectLevel(Long projectId, RoleAssignmentSearchDTO roleAssignmentSearchDTO) {
        ProjectDTO projectDTO = projectMapper.selectByPrimaryKey(projectId);
        RoleVO param = new RoleVO();
        if (roleAssignmentSearchDTO != null) {
            param.setName(roleAssignmentSearchDTO.getRoleName());
        }
        List<Role> roles = roleC7nMapper.listRolesByTenantIdAndLableWithOptions(projectDTO.getOrganizationId(), LabelC7nConstants.PROJECT_ROLE, param);
        List<io.choerodon.iam.api.vo.agile.RoleVO> roleVOList = ConvertUtils.convertList(roles, io.choerodon.iam.api.vo.agile.RoleVO.class);
        List<RoleUserCountVO> roleUserCountVOS = projectUserMapper.countProjectRoleUser(projectId);
        Map<Long, io.choerodon.iam.api.vo.agile.RoleVO> roleMap = roleVOList.stream().collect(Collectors.toMap(RoleDTO::getId, v -> v));
        // 给角色添加用户数
        roleUserCountVOS.forEach(v -> {
            io.choerodon.iam.api.vo.agile.RoleVO roleVO = roleMap.get(v.getRoleId());
            if (roleVO != null) {
                roleVO.setUserCount(v.getUserNumber());
            }
        });
        return roleVOList;
    }

    @Override
    public Page<RoleC7nDTO> listRole(Long tenantId, Long userId, PageRequest pageRequest) {
        List<RoleC7nDTO> roleDTOList = new ArrayList<>();
        Page<UserRoleVO> result = PageHelper.doPageAndSort(pageRequest, () -> roleC7nMapper.selectRoles(1L, "", null, ""));
        result.getContent().forEach(i -> {
            String[] roles = i.getRoleNames().split(",");
            List<RoleNameAndEnabledVO> list = new ArrayList<>(roles.length);
            for (String role : roles) {
                String[] nameAndEnabled = role.split("\\|");
                boolean roleEnabled = true;
                if (nameAndEnabled[2].equals("0")) {
                    roleEnabled = false;
                }
                list.add(new RoleNameAndEnabledVO(nameAndEnabled[0], nameAndEnabled[1], roleEnabled));
            }
            RoleC7nDTO roleC7nDTO = ConvertUtils.convertObject(i, RoleC7nDTO.class);
            roleC7nDTO.setRoles(list);
            if (ResourceLevel.PROJECT.value().equals(i.getLevel())) {
                roleC7nDTO.setTenantId(projectMapper.selectByPrimaryKey(i.getId()).getOrganizationId());
            }
            roleDTOList.add(roleC7nDTO);
        });
        return PageUtils.copyPropertiesAndResetContent(result, roleDTOList);
    }
}
