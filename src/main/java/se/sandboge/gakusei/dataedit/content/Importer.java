package se.sandboge.gakusei.dataedit.content;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class Importer {

    private static final Map lessonNugget = new HashMap<String, Long>();
    static {
        lessonNugget.put("GENKI 1", 21L);
        lessonNugget.put("GENKI 13", 22L);
        lessonNugget.put("GENKI 14", 20L);
        lessonNugget.put("GENKI 15", 24L);
        lessonNugget.put("GENKI 16", 23L);
        lessonNugget.put("GENKI 17", 18L);
        lessonNugget.put("GENKI 18", 16L);
        lessonNugget.put("GENKI 2", 19L);
        lessonNugget.put("GENKI 4", 17L);
        lessonNugget.put("GENKI 9", 15L);
        lessonNugget.put("GU JP1200", 1000L);
        lessonNugget.put("JLPT N1", 12L);
        lessonNugget.put("JLPT N2", 11L);
        lessonNugget.put("JLPT N3", 10L);
        lessonNugget.put("JLPT N4", 14L);
        lessonNugget.put("JLPT N5", 13L);
        lessonNugget.put("KLL 13", 8L);
        lessonNugget.put("KLL 14", 5L);
        lessonNugget.put("KLL 15", 4L);
        lessonNugget.put("KLL 16", 7L);
        lessonNugget.put("KLL 17", 6L);
        lessonNugget.put("KLL 18", 3L);
        lessonNugget.put("KLL 19", 2L);
        lessonNugget.put("KLL 20", 9L);
    }

    private Logger logger = LoggerFactory.getLogger(Importer.class);
    private String prefix;
    private int errCount;
    private String[] props;
    private int propsCount;
    private boolean hasIdProp;
    private int idCount;
    private boolean isLive = false;

    public void readFiles(boolean isLive) {
        this.isLive = isLive;
        String name;
        try (
                InputStream fis = Importer.class.getResourceAsStream("/files.txt");
                InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                BufferedReader br = new BufferedReader(isr)
        ) {
            while ((name = br.readLine()) != null) {
                String primarySeparator = br.readLine();
                String secondarySeparator = br.readLine();
                if (primarySeparator == null || secondarySeparator == null) {
                    logger.error("Malformed file specification!");
                    break;
                }
                readFile(name, primarySeparator, secondarySeparator);
            }
        } catch (IOException e) {
            logger.error("Error", e);
        }
    }

    private void readFile(String fileName, String primarySeparator, String secondarySeparator) {
        String line;
        boolean firstLine = true;
        prefix = fileName.substring(1, fileName.indexOf('.'));
        try (
                InputStream fis = Importer.class.getResourceAsStream(fileName);
                InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                BufferedReader br = new BufferedReader(isr)
        ) {
            boolean writeError = true;
            while ((line = br.readLine()) != null) {
                Map<String, Object> nugget = new HashMap<>();
                if (firstLine) {
                    firstLine = false;
                    processFirstLine(line, primarySeparator);
                    continue;
                }
                processLine(line, primarySeparator, secondarySeparator, nugget, writeError);
                if (errCount == 10 && writeError) {
                    writeError = false;
                    logger.warn("Too many errors, further errors will fail silently for processing of " + fileName);
                }
            }
        } catch (IOException e) {
            logger.error("",e);
        }
    }

    private void processFirstLine(String line, String separator) {
        props = line.split(Pattern.quote(separator));
        propsCount = props.length;
        errCount = 0;
        hasIdProp = false;
        logger.info(line + " : " + props.length);
        for (String prop : props) {
            if ("id".equals(prop)) {
                hasIdProp = true;
                break;
            }
        }
    }

    private void processLine(String line, String primary, String secondary, Map<String, Object> nugget, boolean writeError) {
        String[] items = line.split(Pattern.quote(primary));
        if (items.length != propsCount && !(items.length == propsCount - 1 && line.endsWith(primary))) {
            errCount++;
            if (writeError) {
                logger.warn("Error on line: " + line + " : " + items.length);
            }
            return;
        }
        for (int i = 0; i < items.length; i++) {
            switch (props[i]) {
                case "id":
                    nugget.put("id", generateId(items[i]));
                    break;
                case "state":
                    if (!"hidden".equals(items[i])) {
                        nugget.put(props[i], items[i]);
                    }
                    break;
                case "jnlp":
                    nugget.put(props[i], items[i]);
                    break;
                default:
                    if (!items[i].equals("")) {
                        List<String> value = handleFact(items[i], secondary);
                        nugget.put(props[i], value);
                    }
                    break;
            }
        }
        if (!hasIdProp) {
            nugget.put("id", generateId(""));
        }
        saveNugget(nugget);
    }

    private void saveNugget(Map<String, Object> nuggetMap) {
        List<String> readings = (List<String>)nuggetMap.getOrDefault("reading", Collections.EMPTY_LIST);
        List<String> writings = (List<String>)nuggetMap.getOrDefault("writing", Collections.EMPTY_LIST);
        List<String> swedishs = (List<String>)nuggetMap.getOrDefault("swedish", Collections.EMPTY_LIST);
        List<String> englishs = (List<String>)nuggetMap.getOrDefault("english", Collections.EMPTY_LIST);
        List<String> genkis = (List<String>)nuggetMap.getOrDefault("genki", Collections.EMPTY_LIST);
        List<String> klls = (List<String>)nuggetMap.getOrDefault("kll", Collections.EMPTY_LIST);
        List<String> gus = (List<String>)nuggetMap.getOrDefault("gu", Collections.EMPTY_LIST);
        List<String> jlpts = (List<String>)nuggetMap.getOrDefault("jlpt", Collections.EMPTY_LIST);

        String english = "";
        if (englishs.size() > 0) {
            english = englishs.get(0);
        }
        String id = nuggetMap.get("id").toString();
        List<String> types = (List<String>)nuggetMap.get("type");
        String description = nuggetMap.getOrDefault("description", english).toString();
        Nugget nugget = new Nugget(id, types.get(0), description, true);

        logger.info(nugget.toString());

        for (String r : readings) {
            Fact readingFact = new Fact(0, "reading", r, english, id);
            logger.info(readingFact.toString());
        }
        for (String r : writings) {
            Fact readingFact = new Fact(0, "writing", r, english, id);
            logger.info(readingFact.toString());
        }
        for (String r : swedishs) {
            Fact readingFact = new Fact(0, "swedish", r, english, id);
            logger.info(readingFact.toString());
        }
        for (String r : englishs) {
            Fact readingFact = new Fact(0, "english", r, english, id);
            logger.info(readingFact.toString());
        }
        for (String r : genkis) {
            Fact readingFact = new Fact(0, "genki", r, english, id);
            LessonNugget lessonNugget = new LessonNugget(getGenki(r), id);
            logger.info(lessonNugget.toString());
            logger.info(readingFact.toString());
        }
        for (String r : klls) {
            Fact readingFact = new Fact(0, "kll", r, english, id);
            LessonNugget lessonNugget = new LessonNugget(getKlls(r), id);
            logger.info(lessonNugget.toString());
            logger.info(readingFact.toString());
        }
        for (String r : gus) {
            Fact readingFact = new Fact(0, "gu", r, english, id);
            LessonNugget lessonNugget = new LessonNugget(getGus(r), id);
            logger.info(lessonNugget.toString());
            logger.info(readingFact.toString());
        }
        for (String r : jlpts) {
            Fact readingFact = new Fact(0, "jlpt", r, english, id);
            LessonNugget lessonNugget = new LessonNugget(getJlpt(r), id);
            logger.info(lessonNugget.toString());
            logger.info(readingFact.toString());
        }

    }

    private long getGenki(String r) {
        try {
            return (long)lessonNugget.get("GENKI " + r.toUpperCase());
        } catch (NullPointerException e) {
            logger.error("Missing link: {}", r);
            throw e;
        }
    }

    private long getJlpt(String r) {
        try {
            return (long)lessonNugget.get("JLPT " + r.toUpperCase());
        } catch (NullPointerException e) {
            logger.error("Missing link: {}", r);
            throw e;
        }
    }

    private long getKlls(String r) {
        try {
            return (long)lessonNugget.get("KLL " + r.toUpperCase());
        } catch (NullPointerException e) {
            logger.error("Missing link: {}", r);
            throw e;
        }
    }

    private long getGus(String r) {
        try {
            return (long)lessonNugget.get("GU " + r.toUpperCase());
        } catch (NullPointerException e) {
            logger.error("Missing link: {}", r);
            throw e;
        }
    }

    private List<String> handleFact(String value, String separator) {
        String[] values = value.split(Pattern.quote(separator));
        for (int i = 0; i < values.length; i++) {
            values[i] = values[i].trim();
        }
        return new ArrayList<>(Arrays.asList(values));

    }

    private String generateId(String id) {
        idCount ++;
        if (id.equals("")) {
            return prefix + idCount;
        } else {
            return id;
        }
    }
}
