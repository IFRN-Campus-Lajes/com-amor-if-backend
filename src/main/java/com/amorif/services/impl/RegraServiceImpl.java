package com.amorif.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.amorif.entities.Regra;
import com.amorif.entities.Role;
import com.amorif.entities.User;
import com.amorif.repository.RegraRepository;
import com.amorif.repository.RoleRepository;
import com.amorif.services.RegraService;

@Service
public class RegraServiceImpl implements RegraService{
	
	@Autowired
    private RegraRepository regraRepository;
	
	@Autowired
    private RoleRepository roleRepository;

    public List<Regra> listarTodas() {
        return regraRepository.findByAtivoTrue();
    }
    
    public List<Regra> listarRegrasPermitidasParaUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            List<String> userRolesNames = user.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .collect(Collectors.toList());
            List<Role> userRoles = roleRepository.findByNameIn(userRolesNames);

            // Filtra as regras, excluindo aquelas que possuem a ROLE_SISTEMA
            return regraRepository.findByRolesInAndAtivoTrue(userRoles).stream()
                    .filter(regra -> regra.getRoles().stream()
                            .noneMatch(role -> "ROLE_SISTEMA".equals(role.getName())))
                    .collect(Collectors.toList());
        } else {
            throw new IllegalStateException("Usuário não autenticado ou tipo inesperado de principal");
        }
    }
    
}
