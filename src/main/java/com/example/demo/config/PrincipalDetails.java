package com.example.demo.config;


import com.example.demo.dto.OAuthUserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PrincipalDetails implements UserDetails, OAuth2User {


    private OAuthUserDto oauserDto;

    public PrincipalDetails(OAuthUserDto oauserDto){
        this.oauserDto = oauserDto;
    }
    //-----------------------------------
    //OAUTH2
    //-----------------------------------
    private String accessToken;
    private Map<String,Object> attributes;
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    @Override
    public String getName() {
        return oauserDto.getUsername();
    }
    //-----------------------------------




    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList();
        collection.add(new SimpleGrantedAuthority(oauserDto.getRole()));

        return collection;
    }

    @Override
    public String getPassword() {
        return oauserDto.getPassword();
    }

    @Override
    public String getUsername() {
        return oauserDto.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


}
