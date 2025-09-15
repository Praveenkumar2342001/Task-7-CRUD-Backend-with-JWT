package com.example.CRUD.with.JWT.authentication.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CRUD.with.JWT.authentication.model.Post;
import com.example.CRUD.with.JWT.authentication.repository.PostRepository;
import com.example.CRUD.with.JWT.authentication.security.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostRepository postRepository;
    private final JwtUtil jwtUtil;

    public PostController(PostRepository postRepository, JwtUtil jwtUtil) {
        this.postRepository = postRepository;
        this.jwtUtil = jwtUtil;
    }

    private Claims getClaims(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7); // remove "Bearer "
        return jwtUtil.validateToken(token);
    }

    // Create a new post
    @PostMapping("/insert")
    public Post createPost(@RequestBody Post post, HttpServletRequest request) {
        Claims claims = getClaims(request);
        post.setCreatedBy(claims.getSubject()); // Set username from JWT
        return postRepository.save(post);
    }

    // Get all posts (Admin sees all, User sees their own)
    @GetMapping("/getall")
    public List<Post> getPosts(HttpServletRequest request) {
        Claims claims = getClaims(request);
        String role = claims.get("role", String.class);

        if ("ROLE_ADMIN".equals(role)) {
            return postRepository.findAll();
        }
        return postRepository.findByCreatedBy(claims.getSubject());
    }

    // Get a single post by ID
    @GetMapping("/{id}")
    public Optional<Post> getPost(@PathVariable String id, HttpServletRequest request) {
        Claims claims = getClaims(request);
        Optional<Post> post = postRepository.findById(id);

        if (post.isPresent()) {
            if (!post.get().getCreatedBy().equals(claims.getSubject()) 
                && !"ROLE_ADMIN".equals(claims.get("role", String.class))) {
                throw new RuntimeException("Access Denied");
            }
        }
        return post;
    }

    // Update a post
    @PutMapping("/{id}")
    public Post updatePost(@PathVariable String id, 
                           @RequestBody Post updatedPost, 
                           HttpServletRequest request) {
        Claims claims = getClaims(request);
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getCreatedBy().equals(claims.getSubject()) 
            && !"ROLE_ADMIN".equals(claims.get("role", String.class))) {
            throw new RuntimeException("Access Denied");
        }

        post.setTitle(updatedPost.getTitle());
        post.setContent(updatedPost.getContent());
        return postRepository.save(post);
    }

    // Delete a post
    @DeleteMapping("/{id}")
    public String deletePost(@PathVariable String id, HttpServletRequest request) {
        Claims claims = getClaims(request);
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getCreatedBy().equals(claims.getSubject()) 
            && !"ROLE_ADMIN".equals(claims.get("role", String.class))) {
            throw new RuntimeException("Access Denied");
        }

        postRepository.delete(post);
        return "Post deleted successfully";
    }
}
