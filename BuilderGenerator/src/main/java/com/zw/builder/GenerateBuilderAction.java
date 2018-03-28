package com.zw.builder;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.ui.CollectionListModel;
import com.zw.builder.utils.BuilderUtil;

import java.util.List;

public class GenerateBuilderAction extends AnAction {

    private static String className;
    private static String BUILDER_CLASS_NAME = "Builder";
    private static PsiElementFactory psiElementFactory ;

    private static final String ENTER = "\n";

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        generateBuilder(this.getPsiMethodFromContext(anActionEvent));
    }

    private PsiClass getPsiMethodFromContext(AnActionEvent e) {
        PsiElement elementAt = this.getPsiElement(e);
        return elementAt == null ? null : PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
    }

    private PsiElement getPsiElement(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (psiFile != null && editor != null) {
            int offset = editor.getCaretModel().getOffset();
            return psiFile.findElementAt(offset);
        } else {
            e.getPresentation().setEnabled(false);
            return null;
        }
    }

    private void generateBuilder(final PsiClass psiMethod) {
        className = psiMethod.getName();
        psiElementFactory = JavaPsiFacade.getElementFactory(psiMethod.getProject());
        (new WriteCommandAction.Simple(psiMethod.getProject(), new PsiFile[]{psiMethod.getContainingFile()}) {
            protected void run() throws Throwable {
                    createBuilder(psiMethod);
            }
        }).execute();
    }


    private void createBuilder(PsiClass psiClass) {
        PsiClass builderClass = createBuilderClass(psiClass);
        PsiType builderType = psiElementFactory.createTypeFromText(BUILDER_CLASS_NAME, null);
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(builderClass.getProject());
        List<PsiField> fields = (new CollectionListModel( psiClass.getFields())).getItems();
        //构造方法
        PsiMethod consMethod = psiElementFactory.createConstructor(builderType.getPresentableText());
        PsiUtil.setModifierProperty(consMethod,PsiModifier.PUBLIC,true);
        PsiParameter constructorParameter = psiElementFactory.createParameter("builder", builderType);
        consMethod.getParameterList().add(constructorParameter);
        PsiCodeBlock constructorBody = consMethod.getBody();
        PsiStatement consStatement;
        if (!fields.isEmpty()) {
            for (PsiField field : fields){
                if (!field.hasModifierProperty(PsiModifier.FINAL)) {
                    PsiType fieldType = field.getType();
                    String fieldName = field.getName();
                    PsiField newField = elementFactory.createField(fieldName, field.getType());
                    builderClass.add(newField);

                    String methodName = String.format("set%s", BuilderUtil.capitalize(fieldName));
                    PsiMethod setterMethod = elementFactory.createMethod(methodName, builderType);
                    setterMethod.getModifierList().setModifierProperty(PsiModifier.PUBLIC, true);
                    PsiParameter setterParameter = psiElementFactory.createParameter(fieldName, fieldType);
                    setterMethod.getParameterList().add(setterParameter);
                    PsiCodeBlock setterMethodBody = setterMethod.getBody();
                    if (setterMethodBody != null) {
                        final String actualFieldName = "this." + fieldName;
                        final PsiStatement assignStatement = psiElementFactory.createStatementFromText(String.format(
                                "%s = %s;", actualFieldName, fieldName), setterMethod);
                        setterMethodBody.add(assignStatement);
                        setterMethodBody.add(BuilderUtil.createReturnThis(psiElementFactory, setterMethod));
                    }
                    builderClass.add(setterMethod);
                    //构造方法 内容
                    consStatement = psiElementFactory.createStatementFromText("this." + fieldName + "= builder." + fieldName+";"+ENTER, consMethod);
                    constructorBody.add(consStatement);
                }
            }
            //Build()方法
            PsiType classType = psiElementFactory.createTypeFromText(psiClass.getName(), null);
            PsiMethod buildMethod = elementFactory.createMethod("build", classType);
            PsiCodeBlock body = buildMethod.getBody();
            PsiStatement assignStatement = psiElementFactory.createStatementFromText("return new "+psiClass.getName()+"(this);",buildMethod);
            body.add(assignStatement);
            builderClass.add(buildMethod);
            //添加构造方法
            psiClass.add(consMethod);
        }
    }

    private PsiClass createBuilderClass(final PsiClass targetClass) {
        final PsiClass builderClass = (PsiClass) targetClass.add(psiElementFactory.createClass(BUILDER_CLASS_NAME));
        PsiUtil.setModifierProperty(builderClass, PsiModifier.STATIC, true);
        PsiUtil.setModifierProperty(builderClass, PsiModifier.FINAL, true);
        PsiUtil.setModifierProperty(builderClass, PsiModifier.PUBLIC, false);
        return builderClass;
    }
}
