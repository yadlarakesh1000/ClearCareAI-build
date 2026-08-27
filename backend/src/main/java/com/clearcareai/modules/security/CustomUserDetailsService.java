package com.clearcareai.modules.security;

import java.util.Collections;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.clearcareai.modules.auth.entity.User;
import com.clearcareai.modules.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService{
  private final UserRepository userRepository;
  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Optional<User> userOptional =userRepository.findByEmail(email);
       if(!userOptional.isPresent()){
        throw new UsernameNotFoundException("User details not found ");
       }

         User user = userOptional.get();
    
      GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().name());
      
        return org.springframework.security.core.userdetails.User.builder().username(email).password(user.getPassword()).disabled(!Boolean.TRUE.equals(user.getIsActive())).authorities(Collections.singletonList(authority)).build();
     

  }
  


  
}
