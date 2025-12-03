package com.app.auth.auth_app_backend.services;

import com.app.auth.auth_app_backend.dtos.UserDto;
import com.app.auth.auth_app_backend.entities.Provider;
import com.app.auth.auth_app_backend.entities.User;
import com.app.auth.auth_app_backend.exceptions.ResourceNotFoundException;
import com.app.auth.auth_app_backend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private   final UserRepository userRepository;
    private  final ModelMapper  modelMapper;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
            if (userDto.getEmail() == null ||
                    userRepository.existsByEmail(userDto.getEmail()) ||
                    userDto.getEmail().isBlank()  )
                throw   new IllegalArgumentException("Check Email  Try Again") ;

            User user =modelMapper.map(userDto , User.class);
            user.setProvider(userDto.getProvider()!= null ? userDto.getProvider() : Provider.LOCAL);
            User  saverdUser= userRepository.save(user);
            return modelMapper.map(saverdUser ,  UserDto.class);


    }

    @Override
    public UserDto getUserByEmail(String mail) {
        User user=  userRepository.findByEmail(mail).orElseThrow(()->new ResourceNotFoundException("Mail Not found"));
        return  modelMapper.map(user , UserDto.class);
    }

    @Override
    public UserDto updateUser(UserDto userDto, String userId) {
        return null;
    }

    @Override
    public void deleteUser(String userId) {

    }

    @Override
    public UserDto getUserById(String userId) {
        return null;
    }

    @Override
    @Transactional
    public Iterable<UserDto> getAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .map((user) -> modelMapper.map(user, UserDto.class))
                .toList();
    }
}
