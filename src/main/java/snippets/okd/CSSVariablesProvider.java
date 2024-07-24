package snippets.okd;

import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.codeInsight.template.TemplateContextType;
import org.jetbrains.annotations.NotNull;

public class CSSVariablesProvider extends TemplateContextType {
    protected CSSVariablesProvider() {
        super("OKD_CSS_VARIABLES");
    }

    @Override
    public boolean isInContext(@NotNull TemplateActionContext templateActionContext) {
        String fileName = templateActionContext.getFile().getName();
        return fileName.endsWith(".less") || fileName.endsWith(".css") || fileName.endsWith(".scss");
    }
}