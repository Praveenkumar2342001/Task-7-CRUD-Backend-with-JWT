package com.example.CRUD.with.JWT.authentication.repository;

import com.example.CRUD.with.JWT.authentication.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findByCreatedBy(String username);
}
