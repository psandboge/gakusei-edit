package se.sandboge.gakusei.dataedit.content;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

public class Importer {
    private Logger logger = LoggerFactory.getLogger(Importer.class);
    private String prefix;
    private int errCount;
    private String[] props;
    private int propsCount;
    private boolean hasIdProp;
    private int idCount;

    public void readFiles(String fileName) {
        String name;
        try (
                InputStream fis = Importer.class.getResourceAsStream(fileName);
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
        String id = nuggetMap.get("id").toString();
        String type = nuggetMap.get("type").toString();
        String description = nuggetMap.get("description").toString();
        Nugget nugget = new Nugget(id, type, description, false);
    }

    private List<String> handleFact(String value, String separator) {
        String[] values = value.split(Pattern.quote(separator));
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
