package commands;


import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;

@Creatable
@Singleton
public class FunctionCalls
{
    @Inject
    private ReadJavaDocCommand readJavaDocCommand;
    
    @Function(name="getJavaDoc", description="Получить JavaDoc для данного модуля компиляции. Например, класс B, определенный как тип-член класса A в пакете x.y, должен иметь полное имя \"x.y.A.B\". Обрати внимание, что для того, чтобы его можно было найти, имя типа (или его имя верхнего уровня, охватывающего тип) должно совпадать с названием модуля компиляции, соответствующему ему.", type="object")
    public String getJavaDoc(
            @FunctionParam(name="fullyQualifiedName", description="Полное имя модуля компиляции", required=true) String fullyQualifiedClassName)
    {
        return readJavaDocCommand.getClassAttachedJavadoc( fullyQualifiedClassName );
    }
    @Function(name="getSource", description="Получить исходный код указанного класса.", type="object")
    public String getSource(
            @FunctionParam(name="fullyQualifiedClassName", description="Полное имя Java класса", required=true) String fullyQualifiedClassName)
    {
        return readJavaDocCommand.getClassAttachedSource( fullyQualifiedClassName );
    }

}
