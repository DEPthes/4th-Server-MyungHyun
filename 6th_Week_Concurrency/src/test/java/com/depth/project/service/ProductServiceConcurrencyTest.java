package com.depth.project.service;

import com.depth.project.entity.Product;
import com.depth.project.repository.ProductRepository;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProductServiceConcurrencyTest {

  @Autowired
  private ProductService productService;

  @Autowired
  private ProductRepository productRepository;

  private Long productId;

  @BeforeEach
  void setUp() {
    // 각 테스트 전에 상품 데이터 생성
    Product product = new Product("Test Concurrent Product", 10);
    Product savedProduct = productRepository.save(product);
    productId = savedProduct.getId();
  }

  @AfterEach
  void tearDown() {
    // 각 테스트 후에 상품 데이터 삭제
    if (productId != null) {
      productRepository.deleteById(productId);
    }
  }

  @Test
  void testDecreaseStockWithOptimisticLock_Concurrency() throws InterruptedException {
    int numberOfThreads = 5; // 동시 실행 스레드 수
    int initialStock = productRepository.findById(productId).get().getStock();
    ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
    CountDownLatch latch = new CountDownLatch(numberOfThreads); // 모든 스레드가 작업을 마칠 때까지 대기
    AtomicInteger optimisticLockExceptionsCount = new AtomicInteger(0);

    for (int i = 0; i < numberOfThreads; i++) {
      executorService.submit(() -> {
        try {
          productService.decreaseStockWithOptimisticLock(productId, 1);
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
          System.out.println("Optimistic lock exception caught by thread: " + Thread.currentThread().getName());
          optimisticLockExceptionsCount.incrementAndGet();
        } catch (Exception e) {
          System.err
              .println("Unexpected exception in thread " + Thread.currentThread().getName() + ": " + e.getMessage());
          e.printStackTrace();
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await(); // 모든 스레드가 종료될 때까지 대기
    executorService.shutdown();

    Product finalProduct = productRepository.findById(productId).orElseThrow();
    System.out.println("Initial stock: " + initialStock);
    System.out.println("Final stock: " + finalProduct.getStock());
    System.out.println("OptimisticLockExceptions: " + optimisticLockExceptionsCount.get());

    // 모든 요청이 성공하거나, 일부가 OptimisticLockException으로 실패해야 함
    // 정확히 몇 개의 예외가 발생할지는 실행 환경과 타이밍에 따라 달라질 수 있음
    // 중요한 것은 재고가 음수가 되거나 예상치 못한 값으로 변경되지 않는 것과, 충돌이 감지되는 것을 확인하는 것.
    assertTrue(optimisticLockExceptionsCount.get() < numberOfThreads && optimisticLockExceptionsCount.get() >= 0);
    assertEquals(initialStock - (numberOfThreads - optimisticLockExceptionsCount.get()), finalProduct.getStock());
  }
}