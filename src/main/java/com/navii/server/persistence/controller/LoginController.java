package com.navii.server.persistence.controller;

import com.navii.server.persistence.domain.Voyager;
import com.navii.server.persistence.domain.VoyagerResponse;
import com.navii.server.persistence.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by JMtorii on 2015-10-12.
 */
@RestController
@RequestMapping(value = "/login")
public class LoginController {

    @Autowired
    @Qualifier("loginServiceImpl")
    private LoginService loginService;

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<VoyagerResponse> getVoyagerResponse(@RequestBody Voyager voyager) {
        VoyagerResponse response = loginService.Login(voyager.getEmail(), voyager.getPassword());

        if (response != null) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @RequestMapping(value = "/fb/{accessToken}", method = RequestMethod.POST)
    public ResponseEntity<String> getTokenViaFB(@PathVariable String accessToken) {
        String token = loginService.LoginFromFacebook(accessToken);

        if (token != "") {
            return new ResponseEntity<>(token, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
}
