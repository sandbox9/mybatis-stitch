package sandbox9.mybatis.stitch.preference;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import sandbox9.mybatis.stitch.Activator;

public class StitchPropertyPage extends FieldEditorPreferencePage implements IWorkbenchPropertyPage {

	private IAdaptable element;

	public StitchPropertyPage() {
		super(GRID);
	}

	public void createFieldEditors() {
		addField(new RadioGroupFieldEditor(
				"DatabaseType",
				"Select Database type for mybatis-stitch query",
				1,
				new String[][] { { "mysql", "mysql" }, { "oracle", "oracle" } },
				getFieldEditorParent()));
		addField(new StringFieldEditor("url", "database URL :",
				getFieldEditorParent()));
		addField(new StringFieldEditor("username", "database username :",
				getFieldEditorParent()));
		addField(new StringFieldEditor("password", "database password :",
				getFieldEditorParent()));
	}

	@Override
	public IAdaptable getElement() {
		return element;
	}

	@Override
	public void setElement(IAdaptable element) {
		this.element = element;
		setPreferenceStore(new ScopedPreferenceStore(new ProjectScope(
				getProject()), Activator.PLUGIN_ID));
	}

	protected IProject getProject() {
		return element instanceof IJavaProject ? ((IJavaProject) element)
				.getProject() : (IProject) element;
	}
}