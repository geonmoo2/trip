package com.example.demo.config;


import com.example.demo.Entity.User;
import com.example.demo.dto.OAuthUserDto;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PrincipalDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOption =userRepository.findByUserName(username);

        if(userOption.isEmpty()){
            throw new UsernameNotFoundException(username);
        }
        //Entity -> Dto
        OAuthUserDto dto = new OAuthUserDto();
        User user = userOption.get();
        dto.setUsername(user.getUserName());
        dto.setPassword(user.getPassword());
        dto.setRole(user.getRole());

        //
        return new PrincipalDetails(dto);
    }
}
