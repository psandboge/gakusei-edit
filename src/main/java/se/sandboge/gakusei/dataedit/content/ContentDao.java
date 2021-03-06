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
        List<Lesson> query = jdbcTemplate.query("SELECT id, name, description FROM contentschema.lessons ORDER BY name"
                , new Object[]{}, (rs, rowNum) ->
                        new Lesson(rs.getLong("id")
                                , rs.getString("name")
                                , rs.getString("description")));
        query.forEach(customer -> logger.info(customer.toString()));
        return query;
    }

    public void addLesson(int id, String name, String description) {
        jdbcTemplate.update("INSERT INTO contentschema.lessons(id, name, description) VALUES (?,?,?)"
                , id, name, description);
    }

    public List<Nugget> findExistingNuggets(String type, String reading, String writing) {
        List<Nugget> result = null;
        if ("vocabulary".equals(type)) {
//            SELECT id, type, description, hidden FROM contentschema.nuggets as n
//            WHERE id in (SELECT nuggetid FROM contentschema.facts f WHERE n.id = nuggetid
//                    AND f.data = 'たべる' AND f.type = 'reading')
//            AND id in (SELECT nuggetid FROM contentschema.facts f WHERE n.id = nuggetid
//                    AND f.data = '食べる' AND f.type = 'writing')
            String searchtype = writing.isEmpty() ? "reading" : "writing";
            String term = writing.isEmpty() ? reading : writing;
            logger.info("Search type {}, term {}", searchtype, term);
            result = jdbcTemplate.query(
                    "SELECT n.id, n.type, n.description, n.hidden " +
                    " FROM contentschema.nuggets n, contentschema.facts f " +
                    " WHERE n.id = f.nuggetid AND f.type = ? AND f.data = ?",
                    new Object[] {searchtype, term}, (rs, rowNum) ->
                    new Nugget(rs.getString("id")
                            , rs.getString("type")
                            , rs.getString("description")
                            , rs.getBoolean("hidden")));
        } else {
            result = jdbcTemplate.query("SELECT id, type, description, hidden FROM contentschema.nuggets as n" +
                    " WHERE type = 'kanji' AND id in (SELECT nuggetid FROM contentschema.facts WHERE n.id = nuggetid " +
                    " AND f.data = ? AND f.type = 'reading')" +
                    " AND id in (SELECT nuggetid FROM contentschema.facts f WHERE n.id = nuggetid " +
                    " AND f.data = ? AND f.type = 'writing')", new Object[] {reading, writing}, (rs, rowNum) ->
                    new Nugget(rs.getString("id")
                            , rs.getString("type")
                            , rs.getString("description")
                            , rs.getBoolean("hidden")));
        }
        result.forEach(customer -> logger.info(customer.toString()));
        return result;
    }

    public List<Fact> findFactsForNugget(String nuggetId) {
        return jdbcTemplate.query("SELECT id, type, data, description, nuggetid " +
                " FROM contentschema.facts as n" +
                " WHERE nuggetid = ?"
                , new Object[] {nuggetId}, (rs, rowNum) ->
                new Fact(rs.getLong("id")
                        , rs.getString("type")
                        , rs.getString("data")
                        , rs.getString("description")
                        , rs.getString("hidden")));
    }

    public List<Lesson> findConnectedLessonsForNugget(String nuggetId) {
        List<Lesson> query = jdbcTemplate.query("SELECT id, name, description FROM contentschema.lessons " +
                        " WHERE id in (SELECT lesson_id FROM contentschema.lessons_nuggets ln WHERE ln.nugget_id = ? )" +
                        " ORDER BY name"
                , new Object[]{nuggetId}, (rs, rowNum) ->
                        new Lesson(rs.getLong("id")
                                , rs.getString("name")
                                , rs.getString("description")));
        query.forEach(customer -> logger.info(customer.toString()));
        return query;
    }

    public void createNugget(Nugget nugget) {
        jdbcTemplate.update("INSERT INTO contentschema.nuggets(id, type, description, hidden) VALUES (?,?,?,?)"
                , nugget.getId(), nugget.getType(), nugget.getDescription(), nugget.getHidden());
    }

    public void createFact(Fact fact) {
        jdbcTemplate.update("INSERT INTO contentschema.facts(type, data, description, nuggetid) VALUES (?,?,?,?)"
        , fact.getType(), fact.getData(), fact.getDescription(), fact.getNuggetid());
    }

    public void createLessonNugget(LessonNugget lessonNugget) {
        jdbcTemplate.update("INSERT INTO contentschema.lessons_nuggets(lesson_id, nugget_id) VALUES (?,?)"
                , lessonNugget.getLessonId(), lessonNugget.getNuggetId());
    }
}
