import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.project.ProjectAccessor;

public class InspectAstahProject {
    public static void main(String[] args) throws Exception {
        ProjectAccessor accessor = AstahAPI.getAstahAPI().getProjectAccessor();
        accessor.open(args[0]);
        IModel project = accessor.getProject();
        System.out.println("Project: " + project.getName());
        walk(project, "");
        accessor.close();
    }

    private static void walk(INamedElement e, String indent) throws Exception {
        System.out.println(indent + e.getClass().getSimpleName() + " " + e.getName());
        for (IDiagram d : e.getDiagrams()) {
            System.out.println(indent + "  diagram " + d.getName() + " presentations=" + d.getPresentations().length);
        }
        if (e instanceof IPackage) {
            for (INamedElement child : ((IPackage) e).getOwnedElements()) {
                walk(child, indent + "  ");
            }
        }
    }
}
