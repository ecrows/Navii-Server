package com.navii.server.persistence.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navii.server.Application;
import com.navii.server.persistence.domain.User;
import com.navii.server.util.ObjectMapperFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Random;

/**
 * Created by JMtorii on 2015-11-27.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest(randomPort = true)
@TestPropertySource(properties = "spring.datasource.url=jdbc:mysql://naviappdbinstance.cmd4kpxqni0s.us-east-1.rds.amazonaws.com:3306/naviDB_test")
public class UserControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(UserControllerTest.class);

    private MockMvc mvc;
    private ObjectMapper objectMapper;
    private Random random;

    @Autowired
    private WebApplicationContext wac;

    @Before
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();
        objectMapper = ObjectMapperFactory.createMapper();
        random = new Random();
    }

    @Test
    public void createUserFailsDueToInsufficientData() throws Exception {
        User user = new User.Builder()
                .username("userDoesNotExist")
                .build();

        sendCreateUserRequest(user)
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void getUserFailsDueToMissingUser() throws Exception {
        sendGetUserRequest("someMissingUser")
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void createUserSucceeds() throws Exception {
        // just a precaution
        sendDeleteAllUserRequest()
                .andExpect(MockMvcResultMatchers.status().isOk());

        int randomId = random.nextInt(1000);

        User user = new User.Builder()
                .username("user-test_" + randomId)
                .password("password-test_" + randomId)
                .salt("salt-test_" + randomId)
                .isFacebook(false)
                .build();

        sendCreateUserRequest(user)
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void deleteAllUsersSucceeds() throws Exception {
        sendDeleteAllUserRequest()
                .andExpect(MockMvcResultMatchers.status().isOk());

        MvcResult result = sendGetAllUsersRequest()
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        List<User> users = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<User>>(){});
        Assert.assertEquals(0, users.size());
    }

    @Test
    public void signUpFailsDueToBlankUsername() throws Exception {
        String username = "";
        String password = "anyPassword";

        sendSignUpRequest(username, password)
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void signUpFailsDueToMissingUsername() throws Exception {
        String password = "anyPassword";

        mvc.perform(MockMvcRequestBuilders.post("/user/signUp")
                .param("password", password));
    }

    @Test
    public void signUpFailsDueToBlankPassword() throws Exception {
        String username = "anyUsername";
        String password = "";

        sendSignUpRequest(username, password)
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void signUpFailsDueToMissingPassword() throws Exception {
        String username = "anyUsername";

        mvc.perform(MockMvcRequestBuilders.post("/user/signUp")
                .param("username", username));
    }

    @Test
    public void signUpFailsSinceUserAlreadyExists() throws Exception {
        User user = createGenericTestUser();

        sendCreateUserRequest(user)
                .andExpect(MockMvcResultMatchers.status().isCreated());

        sendSignUpRequest(user.getUsername(), user.getPassword())
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    public void signUpSucceeds() throws Exception {
        // just a precaution
        sendDeleteAllUserRequest()
                .andExpect(MockMvcResultMatchers.status().isOk());

        int randomId = random.nextInt(1000);
        String username = "user-test" + randomId;
        String password = "password-test" + randomId;

        sendSignUpRequest(username, password)
                .andExpect(MockMvcResultMatchers.status().isOk());

        MvcResult result = sendGetUserRequest(username)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        User user = objectMapper.readValue(result.getResponse().getContentAsString(), User.class);
        Assert.assertEquals(username, user.getUsername());
        Assert.assertEquals(password, user.getPassword());
        Assert.assertEquals(false, user.isFacebook());
    }

    @Test
    public void loginFailsDueToBlankUsername() throws Exception {
        String username = "";
        String password = "anyPassword";

        sendLoginRequest(username, password)
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void loginFailsDueToMissingUsername() throws Exception {
        String password = "anyPassword";

        mvc.perform(MockMvcRequestBuilders.post("/user/login")
                .param("password", password));
    }

    @Test
    public void loginFailsDueToBlankPassword() throws Exception {
        String username = "anyUsername";
        String password = "";

        sendLoginRequest(username, password)
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void loginFailsDueToMissingPassword() throws Exception {
        String username = "anyUsername";

        mvc.perform(MockMvcRequestBuilders.post("/user/login")
                .param("username", username));
    }

    @Test
    public void loginFailsSinceUsernameAndPasswordDoNotMatch() throws Exception {
        User user = createGenericTestUser();

        sendCreateUserRequest(user)
                .andExpect(MockMvcResultMatchers.status().isCreated());

        sendLoginRequest(user.getUsername(), "someWrongPassword")
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void loginSucceeds() throws Exception {
        // just a precaution
        sendDeleteAllUserRequest()
                .andExpect(MockMvcResultMatchers.status().isOk());

        User user = createGenericTestUser();

        sendCreateUserRequest(user)
                .andExpect(MockMvcResultMatchers.status().isCreated());

        sendLoginRequest(user.getUsername(), user.getPassword())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void updatePasswordSucceeds() throws Exception {
        sendDeleteAllUserRequest()
                .andExpect(MockMvcResultMatchers.status().isOk());

        User user = createGenericTestUser();

        sendCreateUserRequest(user)
                .andExpect(MockMvcResultMatchers.status().isCreated());

        sendUpdatePasswordRequest(user.getUsername(), user.getPassword(), "thisWillSucceed")
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    private User createGenericTestUser() {
        int randomId = random.nextInt(1000);
        String username = "user-test_" + randomId;
        String password = "password-test_" + randomId;

        return new User.Builder()
                .username(username)
                .password(password)
                .salt("salt-test_" + randomId)
                .isFacebook(false)
                .build();
    }

    private ResultActions sendGetUserRequest(String username) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.get(String.format("/user/%s", username)));
    }

    private ResultActions sendGetAllUsersRequest() throws Exception {
        return mvc.perform(MockMvcRequestBuilders.get("/user"));
    }

    private ResultActions sendCreateUserRequest(User user) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post("/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(user)));
    }

    private ResultActions sendDeleteAllUserRequest() throws Exception {
        return mvc.perform(MockMvcRequestBuilders.delete("/user/all"));
    }

    private ResultActions sendSignUpRequest(String username, String password) throws Exception {
        return mvc
                .perform(MockMvcRequestBuilders.post("/user/signUp", username, password)
                .param("username", username)
                .param("password", password));
    }

    private ResultActions sendLoginRequest(String username, String password) throws Exception {
        return mvc
                .perform(MockMvcRequestBuilders.get("/user/login", username, password)
                .param("username", username)
                .param("password", password));
    }

    private ResultActions sendUpdatePasswordRequest(String username, String oldPassword, String newPassword) throws Exception {
        return mvc
                .perform(MockMvcRequestBuilders.put("/user/changePassword", username, oldPassword, newPassword)
                        .param("username", username)
                        .param("oldPassword", oldPassword)
                        .param("newPassword", newPassword));
    }
}
