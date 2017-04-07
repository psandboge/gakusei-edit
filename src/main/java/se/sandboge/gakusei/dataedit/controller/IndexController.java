package se.sandboge.gakusei.dataedit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.sandboge.gakusei.dataedit.content.ContentDao;

@Controller
public class IndexController {

    private final ContentDao contentDao;

    @Autowired
    public IndexController(ContentDao contentDao) {
        this.contentDao = contentDao;
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
}
