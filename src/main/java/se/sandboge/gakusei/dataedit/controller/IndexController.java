package se.sandboge.gakusei.dataedit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.sandboge.gakusei.dataedit.content.ContentDao;
import se.sandboge.gakusei.dataedit.content.Importer;
import se.sandboge.gakusei.dataedit.content.Nugget;

import java.util.Collections;
import java.util.List;

@Controller
public class IndexController {

    private final ContentDao contentDao;
    private final Importer importer;

    @Autowired
    public IndexController(ContentDao contentDao, Importer importer) {
        this.contentDao = contentDao;
        this.importer = importer;
    }


    @RequestMapping("/")
    public String HomeRequest(Model model) {
        model.addAttribute("hej", "Connected to DB version " + contentDao.getVersion());
        model.addAttribute("lessons", contentDao.getLessons());
        return "index";
    }

    @RequestMapping("/doaddlesson")
    public String doAddLesson(
            @RequestParam Integer id,
            @RequestParam String name,
            @RequestParam String description,
            Model model) {
        model.addAttribute("id", id);
        model.addAttribute("name", name);
        model.addAttribute("description", description);
        contentDao.addLesson(id, name, description);
        return "addlesson";
    }

    @RequestMapping("/addlesson")
    public String addLesson(Model model) {
        return "addlesson";
    }

    @RequestMapping("/addword")
    public String addWord(Model model) {
        return "addword";
    }

    @RequestMapping("/dofindword")
    public String addWord(
            @RequestParam(defaultValue = "") String reading,
            @RequestParam(defaultValue = "") String writing,
            Model model) {
        model.addAttribute(reading);
        model.addAttribute(writing);

        List<Nugget> existingNuggets = Collections.EMPTY_LIST;
        if (!reading.equals("") || !writing.equals("")) {
            existingNuggets = contentDao.findExistingNuggets("vocabulary", reading, writing);
        }
        model.addAttribute("existingNuggets", existingNuggets);
        return "addword";
    }

    @RequestMapping("/importfile")
    public String importFile(@RequestParam(defaultValue = "false") boolean isLive) {
        importer.readFiles(isLive);
        return "index";
    }

}
