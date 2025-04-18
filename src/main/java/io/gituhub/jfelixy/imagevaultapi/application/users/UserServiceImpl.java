package io.gituhub.jfelixy.imagevaultapi.application.users;

import io.gituhub.jfelixy.imagevaultapi.application.jwt.JwtService;
import io.gituhub.jfelixy.imagevaultapi.domain.AccessToken;
import io.gituhub.jfelixy.imagevaultapi.domain.entity.User;
import io.gituhub.jfelixy.imagevaultapi.domain.exception.DuplicatedTupleException;
import io.gituhub.jfelixy.imagevaultapi.domain.service.UserService;
import io.gituhub.jfelixy.imagevaultapi.infra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public User save(User user) {
        //Verify if user exists and throws an exception
        var possibleUser = getByEmail(user.getEmail());
        if (possibleUser != null){
            throw new DuplicatedTupleException("User already exists!");
        }

        encodePassword(user);
        return userRepository.save(user);
    }

    @Override
    public AccessToken authenticate(String email, String password) {
        var user = getByEmail(email);
        if(user == null){
            return null;
        }

        if(passwordEncoder.matches(password, user.getPassword())){
            return jwtService.generateToken(user);
        }
        return null;
    }

    private void encodePassword(User user){
        String rawPassword = user.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);
    }
}
