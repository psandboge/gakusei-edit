package se.sandboge.gakusei.dataedit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @RequestMapping("/addlesson")
    public String Addlesson(Model model) {
        model.addAttribute("Hej", "Hej");
        return "addlesson";
    }
}
