package dev.twalidaziz.cms;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class HtmlRenderer {

    private final SpringTemplateEngine templateEngine;

    public HtmlRenderer(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String renderTemplate(String templateName, Model model) {
        Context context = new Context();
        context.setVariables(model.asMap());
        return templateEngine.process(templateName, context);
    }
}
