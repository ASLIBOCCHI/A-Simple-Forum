package com.example.controller;

import com.example.dto.PostRequest;
import com.example.dto.ReplyRequest;
import com.example.model.Post;
import com.example.model.Reply;
import com.example.model.User;
import com.example.repository.PostRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();
        } else {
            username = principal.toString();
        }
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping
    public Post createPost(@RequestBody PostRequest request) {
        User user = getCurrentUser();
        Post post = new Post();
        post.setAuthorId(user.getId());
        post.setAuthorUsername(user.getUsername());
        post.setContent(request.getContent());
        return postRepository.save(post);
    }

    @PostMapping("/{postId}/replies")
    public ResponseEntity<?> addReply(@PathVariable String postId, @RequestBody ReplyRequest request) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (!postOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        User user = getCurrentUser();
        Reply reply = new Reply();
        reply.setAuthorId(user.getId());
        reply.setAuthorUsername(user.getUsername());
        reply.setText(request.getText());

        Post post = postOpt.get();
        post.getReplies().add(reply);
        postRepository.save(post);

        return ResponseEntity.ok(post);
    }
    
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable String postId) {
        User user = getCurrentUser();
        if (!user.getRoles().contains("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admins can delete posts");
        }
        
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isPresent()) {
            postRepository.deleteById(postId);
            return ResponseEntity.ok("Post deleted");
        }
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{postId}/replies/{replyId}")
    public ResponseEntity<?> deleteReply(@PathVariable String postId, @PathVariable String replyId) {
        User user = getCurrentUser();
        if (!user.getRoles().contains("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admins can delete replies");
        }
        
        Optional<Post> postOpt = postRepository.findById(postId);
        if (!postOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Post post = postOpt.get();
        boolean removed = post.getReplies().removeIf(r -> r.getReplyId().equals(replyId));
        if (removed) {
            postRepository.save(post);
            return ResponseEntity.ok("Reply deleted");
        }
        
        return ResponseEntity.notFound().build();
    }
}
