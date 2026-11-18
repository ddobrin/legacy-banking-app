package com.banking.cif.repository;

import com.banking.cif.entity.ProductEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA repository for {@link ProductEntity}. */
public interface ProductRepository extends JpaRepository<ProductEntity, String> {

  /**
   * Finds all active banking products ordered by product code.
   *
   * @return list of active products
   */
  List<ProductEntity> findByActiveTrueOrderByProductCodeAsc();
}
