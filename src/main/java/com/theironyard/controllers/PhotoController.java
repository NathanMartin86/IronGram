package com.theironyard.controllers;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Timer;
import com.theironyard.entities.Photo;
import com.theironyard.entities.User;
import com.theironyard.services.PhotoRepository;
import com.theironyard.services.UserRepository;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import utils.PasswordHash;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimerTask;

/**
 * Created by macbookair on 11/17/15.
 */
@RestController
public class PhotoController {
    @Autowired
    UserRepository users;

    @Autowired
    PhotoRepository photos;

    @RequestMapping ("/login")
    public User login(HttpSession session, HttpServletResponse response, String username, String password) throws Exception {
        User user = users.findOneByUsername(username); // "does this user exist in the database?"
                                                       // That's what this checks.
        if (user == null){
            user = new User();
            user.username = username;
            user.password = PasswordHash.createHash(password);
            users.save(user);
        }
        else if (!PasswordHash.validatePassword(password,user.password)){
            throw new Exception("Wrong Password");
        }
        session.setAttribute("username",username);

        response.sendRedirect("/");
        return user;
    }

    @RequestMapping("/logout")
    public void logout (HttpSession session, HttpServletResponse response) throws IOException {
        session.invalidate();
        response.sendRedirect("/");
    }

    @RequestMapping("/user")
    public User user (HttpSession session){
        String username = (String) session.getAttribute("username");
        if (username == null){
            return null;
        }
        return users.findOneByUsername(username);
    }

    @RequestMapping("/upload")
    public Photo upload(HttpSession session,
                        HttpServletResponse response,
                        String receiver,
                        int userInput,
                        MultipartFile photo,
                        boolean isPublic

    ) throws Exception {
        String username = (String) session.getAttribute("username");
        if (username ==null){
            throw new Exception("Not Logged in");

        }
        User senderUser = users.findOneByUsername(username);
        User receiverUser = users.findOneByUsername(receiver);

        if (receiverUser == null){
            throw new Exception("Receiver name doesn't exist");
        }

        File photoFile = File.createTempFile("photo",".jpg", new File ("public"));
        FileOutputStream fos = new FileOutputStream(photoFile);
        fos.write(photo.getBytes());

        Photo p = new Photo();
        p.sender = senderUser;
        p.receiver = receiverUser;
        p.filename = photoFile.getName();
        p.userInput = userInput;
        p.isPublic = isPublic;
        photos.save(p);

        response.sendRedirect("/");

        return p;
    }

    @RequestMapping("/photos")
    public List<Photo> showPhotos(HttpSession session) throws Exception {
        String username = (String) session.getAttribute("username");
        if (username == null){
            throw new Exception("Not Logged in");
        }

        User user = users.findOneByUsername(username);
        List<Photo> photoList = photos.findByReceiver(user);
        for (Photo p : photoList){
            if (p.accessTime == null){
                p.accessTime = LocalDateTime.now();
                photos.save(p);
            }
            else if (p.accessTime.isBefore(LocalDateTime.now().minusSeconds(p.userInput))){
                photos.delete(p);
                File f = new File ("public",p.filename);
                f.delete();
            }

        }

        return photos.findByReceiver(user);
    }

    @RequestMapping("/public-photos")
        public List<Photo> publicPhotos(String username) throws Exception {
        User user = users.findOneByUsername(username);
        List<Photo> publicList = new ArrayList<>();
        for (Photo p : photos.findBySender(user)){
            if(p.isPublic){
                publicList.add(p);
            }
        }
        return publicList;
    }
}




















