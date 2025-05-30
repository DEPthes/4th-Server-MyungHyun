package com.depth.project.service;

import com.depth.project.entity.Product;
import com.depth.project.repository.ProductRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;

  // 재고 감소 (낙관적 락 미적용 시 동시성 문제 발생 가능)
  @Transactional
  public void decreaseStockProblem(Long productId, int quantity) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("Product not found"));

    if (product.getStock() < quantity) {
      throw new RuntimeException("Not enough stock");
    }
    product.setStock(product.getStock() - quantity);
    productRepository.save(product);
  }

  // 재고 감소 (낙관적 락 적용)
  @Transactional
  public void decreaseStockWithOptimisticLock(Long productId, int quantity) {
    try {
      Product product = productRepository.findById(productId)
          .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

      if (product.getStock() < quantity) {
        throw new RuntimeException("Not enough stock for product: " + product.getName());
      }

      product.setStock(product.getStock() - quantity);
      productRepository.save(product);

    } catch (OptimisticLockException ole) {
      System.out.println("Optimistic lock conflict occurred for product: " + productId + ". Please try again.");
      throw ole;
    }
  }
}