package com.service.authentication_server.repository;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
//import com.service.authentication_server.configuration.CassandraConfiguration;
import com.service.authentication_server.exception.GenericException.GenericException;
import com.service.authentication_server.model.User;
import com.service.authentication_server.model.UserData;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraAdminOperations;
import org.springframework.data.cassandra.core.cql.CqlIdentifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.HashMap;

import static com.service.authentication_server.utils.CryptoUtils.generatePBKDF2;
import static com.service.authentication_server.utils.CryptoUtils.generateSalt;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = CassandraConfiguration.class)
class UserRepositoryTest {

    private static final String KEYSPACE_CREATION_QUERY  = "CREATE KEYSPACE IF NOT EXISTS user_test_keyspace WITH REPLICATION = { 'class': 'SimpleStrategy', 'replication_factor': '1' };";

    private static final String KEYSPACE_ACTIVATE_QUERY = "USE user_test_keyspace";

    private static final String DATA_TABLE_NAME= "user";

    //@Autowired
    //private UserRepository userRepository;

    @Autowired
    private CassandraAdminOperations adminTemplate;

    @BeforeClass
    public static void startCassandraEmbedded() throws InterruptedException, TTransportException, ConfigurationException, IOException {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        Cluster cluster = Cluster.builder().addContactPoint("127.0.0.1").withPort(9142).build();
        System.out.println("Server Started at 127.0.0.1:9142... ");
        Session session = cluster.connect();
        session.execute(KEYSPACE_CREATION_QUERY);
        session.execute(KEYSPACE_ACTIVATE_QUERY);
        Thread.sleep(5000);
    }

    @Before
    void createTable()  throws InterruptedException, TTransportException, ConfigurationException, IOException {
        adminTemplate.createTable(true, CqlIdentifier.of(DATA_TABLE_NAME), User.class, new HashMap<String, Object>());
    }

    @Test
    void testInsertUser(){
        UserData userData = new UserData("João", "joaoamaral@gmail.com", "password123");

        String name = userData.getName();
        String email = userData.getEmail();
        String password = userData.getPassword();

        byte[] salt = generateSalt();
        byte[] passwordHash = new byte[0];
        try {
            passwordHash = generatePBKDF2(password, salt);
        } catch (GenericException e) {
            e.printStackTrace();
        }

        User user = new User(name, email, passwordHash, salt);

        //userRepository.save(user);

    }

    @Test
    void getUserByEmail() {
    }

    @Test
    void deleteByEmail() {
    }

    @After
    public void dropTable(){
        adminTemplate.dropTable(CqlIdentifier.of(DATA_TABLE_NAME));
    }

    @AfterClass
    public static void stopCassandraEmbedded(){
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }

}