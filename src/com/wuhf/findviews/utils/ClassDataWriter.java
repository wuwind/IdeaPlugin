package com.wuhf.findviews.utils;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.wuhf.findviews.beans.ResIdBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ClassDataWriter extends WriteCommandAction {

    private List<ResIdBean> resIdBeans;
    private PsiClass psiClass;
    private PsiFile psiFile;
    private AnActionEvent anActionEvent;

    public ClassDataWriter(AnActionEvent anActionEvent, PsiFile psiFile, List<ResIdBean> resIdBeans, PsiClass psiClass) {
        this(psiFile.getProject(), psiFile);
        this.psiFile = psiFile;
        this.anActionEvent = anActionEvent;
        this.resIdBeans = resIdBeans;
        this.psiClass = psiClass;
    }

    private ClassDataWriter(@Nullable Project project, PsiFile... files) {
        super(project, files);
    }

    @Override
    protected void run(@NotNull Result result) throws Throwable {
        writeFindViews();
    }

    private void writeFindViews() {
        StringBuilder method = new StringBuilder();
        String methodBegin;
        if (isActivity()) {
            methodBegin = "private void initViews(){";
        } else {
            methodBegin = "private void initViews(View view){";
        }
        writeMethod();
        String methodEnd = "}";
        PsiElementFactory psiElementFactory = PsiElementFactory.SERVICE.getInstance(psiFile.getProject());
        for (ResIdBean resIdBean : resIdBeans) {
            if (psiClass.findFieldByName(resIdBean.getFieldName(), false) == null) {
                String field = "private " +
                        resIdBean.getName() +
                        " " +
                        resIdBean.getFieldName() +
                        ";";
                PsiField fieldElement = psiElementFactory.createFieldFromText(field, psiClass);
                psiClass.add(fieldElement);
            }
            PsiMethod[] methods = psiClass.findMethodsByName("initViews", false);
            PsiMethod initViewsMethod = methods.length > 0 ? methods[0] : null;
            if (initViewsMethod == null) {
                psiClass.add(psiElementFactory.createMethodFromText((methodBegin + methodEnd), psiClass));
            }
            methods = psiClass.findMethodsByName("initViews", false);
            initViewsMethod = methods[0];
            PsiCodeBlock body = initViewsMethod.getBody();
            if (body != null && !body.getText().contains(resIdBean.getId())) {
                appendFindViewsMethodBody(method, resIdBean);
            }
        }
        if (method.length() != 0) {
            PsiMethod[] methods = psiClass.findMethodsByName("initViews", false);
            PsiMethod initViewsMethod = methods.length > 0 ? methods[0] : null;
            if (initViewsMethod != null) {
                PsiCodeBlock body = initViewsMethod.getBody();
                if (body != null) {
                    StringBuilder codeBlock = new StringBuilder(body.getText());
                    body.delete();
                    codeBlock.insert(codeBlock.length() - 1, method.toString());
                    initViewsMethod.add(psiElementFactory.createCodeBlockFromText(codeBlock.toString(), initViewsMethod));
                }
            }
        }

        NotifyUtils.showInfo(psiFile.getProject(), "success");
    }

    private void writeMethod() {
        String methodStr;
        if (isActivity()) {
            methodStr = "initViews();";
        } else {
            methodStr = "initViews(view);";
        }
        Editor editor = anActionEvent.getData(DataKeys.EDITOR);
        Project project = anActionEvent.getData(DataKeys.PROJECT);
        Document document = editor.getDocument();
        SelectionModel selectionModel = editor.getSelectionModel();
        int endOffset = selectionModel.getSelectionEnd();
        int curLineNumber = document.getLineNumber(endOffset);
        int nextLineStartOffset = document.getLineStartOffset(curLineNumber + 1);
        int nextLineEndOffset = document.getLineEndOffset(curLineNumber + 1);
        String nextText = document.getText(new TextRange(nextLineStartOffset, nextLineEndOffset));
        nextText = nextText.trim();
        if (!methodStr.equals(nextText)) {
            int lineStartOffset = document.getLineStartOffset(curLineNumber);
            int lineEndOffset = document.getLineEndOffset(curLineNumber);
            String text = document.getText(new TextRange(lineStartOffset, lineEndOffset));
            int dx = text.length() - text.trim().length();
            String writeStr = "\n" + text.substring(0, dx) + methodStr;
            document.insertString(lineEndOffset, writeStr);
            PsiDocumentManager.getInstance(project).commitAllDocuments();
        }
    }

    private void appendFindViewsMethodBody(StringBuilder method, ResIdBean resIdBean) {
        String findViewById;
        if (isActivity()) {
            findViewById = "findViewById(";
        } else {
            findViewById = "view.findViewById(";
        }
        method.append(resIdBean.getFieldName())
                .append(" = ")
                .append("(")
                .append(resIdBean.getName())
                .append(")")
                .append(findViewById)
                .append("R.id.")
                .append(resIdBean.getId())
                .append(");");
    }

    private boolean isActivity() {
        GlobalSearchScope scope = GlobalSearchScope.allScope(psiFile.getProject());
        PsiClass activityClass = JavaPsiFacade.getInstance(psiFile.getProject()).findClass(
                "android.app.Activity", scope);
        return activityClass != null && psiClass.isInheritorDeep(activityClass, null);
    }
}