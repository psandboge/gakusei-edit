package se.sandboge.gakusei.dataedit.content;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ContentDao {
    private Logger logger = LoggerFactory.getLogger(ContentDao.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ContentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        int expectedVersion = 19;
        int version = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM databasechangelog", Integer.class);
        if (version != expectedVersion) {
            logger.error("Wrong database version: {}! (Expected version: {}) You will probably have to update this program.", version, expectedVersion);
            System.exit(expectedVersion);
        }
    }

    public void getLessons() {
        jdbcTemplate.query("SELECT id, name, description FROM contentschema.lessons", new Object[] {}, (rs, rowNum) -> new Lesson(rs.getLong("id"), rs.getString("name"), rs.getString("description"))
        ).forEach(customer -> logger.info(customer.toString()));
    }
}
