package se.sandboge.gakusei.dataedit.content;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class ContentDao {
    private Logger logger = LoggerFactory.getLogger(ContentDao.class);

    private final JdbcTemplate jdbcTemplate;
    private static String version;
    private  final int expectedVersion = 19;

    @Autowired
    public ContentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int getVersion() {
        return expectedVersion;
    }

    @PostConstruct
    public void init() {
        int version = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM databasechangelog", Integer.class);
        if (version != expectedVersion) {
            logger.error("Wrong database version: {}! (Expected version: {}) You will probably have to update this program.", version, expectedVersion);
            System.exit(expectedVersion);
        }
    }

    public List<Lesson> getLessons() {
        List<Lesson> query = jdbcTemplate.query("SELECT id, name, description FROM contentschema.lessons"
                , new Object[]{}, (rs, rowNum) ->
                        new Lesson(rs.getLong("id")
                                , rs.getString("name")
                                , rs.getString("description")));
        query.forEach(customer -> logger.info(customer.toString()));
        return query;
    }

    public void addLesson(String id, String name, String description) {
        jdbcTemplate.update("INSERT INTO contentschema.lessons(id, name, description) VALUES (?,?,?)"
                , id, name, description);
    }
}
