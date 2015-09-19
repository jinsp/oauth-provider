package org.nuvola.oauth.security;

import org.nuvola.oauth.business.Account;
import org.nuvola.oauth.business.User;
import org.nuvola.oauth.repository.AccountRepository;
import org.nuvola.oauth.repository.UserRepository;
import org.nuvola.oauth.shared.ApplicationAuthority;
import org.nuvola.oauth.shared.MyUserDetails;
import org.nuvola.oauth.shared.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MyUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Autowired
    MyUserDetailsService(UserRepository userRepository,
                         AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }

        UserProfile profile = new UserProfile();
        profile.setId(user.getId());
        profile.setUserName(user.getUserName());
        profile.setFirstName(user.getFirstName());
        profile.setLastName(user.getLastName());
        profile.setPassword(user.getPassword());
        profile.setEmail(user.getEmail());

        List<ApplicationAuthority> authorities = new ArrayList<>();
        List<GrantedAuthority> granted = new ArrayList<>();

        for (Account account : accountRepository.findByUser(user)) {
            String clientId = account.getApplication().getClientId();
            String authority = account.getAuthority().getValue();

            authorities.add(new ApplicationAuthority(clientId, authority));
            granted.add(new SimpleGrantedAuthority(authority));
        }

        profile.setAuthorities(authorities);

        return new MyUserDetails(profile, granted);
    }
}
