package com.boozebuddies.repository;

import com.boozebuddies.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  Optional<Category> findById(Long id);

  List<Category> findAll();

  Optional<Category> findByName(String name);
}
