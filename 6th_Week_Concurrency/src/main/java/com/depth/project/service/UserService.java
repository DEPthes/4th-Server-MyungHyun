package com.depth.project.service;

import com.depth.project.entity.User;
import com.depth.project.entity.Order;
import com.depth.project.repository.UserRepository;
import com.depth.project.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final OrderRepository orderRepository;

  @Transactional
  public User signUpAndCreateInitialOrder(String username, String email, int age, String orderNumber,
      String productName) {
    User newUser = new User(username, email, age);
    User savedUser = userRepository.save(newUser);

    if ("error_user".equals(username)) {
      throw new RuntimeException("Simulated error during order creation!");
    }

    Order initialOrder = new Order(orderNumber, productName, savedUser);
    orderRepository.save(initialOrder);

    return savedUser;
  }

  @Transactional
  public void updateUserEmail(Long userId, String newEmail) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    user.setEmail(newEmail);
    userRepository.save(user);

    if (newEmail.contains("error")) {
      throw new RuntimeException("Simulated error after email update!");
    }
  }
}