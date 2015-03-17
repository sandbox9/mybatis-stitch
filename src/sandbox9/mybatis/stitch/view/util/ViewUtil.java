package sandbox9.mybatis.stitch.view.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;

public class ViewUtil {
	public static IJavaProject getJavaProject(IDocument document)
	{
		IStructuredModel model = null;
		String baseLocation = null;
		IJavaProject result = null;

		// try to locate the file in the workspace
		try
		{
			model = StructuredModelManager.getModelManager().getExistingModelForRead(document);
			if (model != null)
			{
				baseLocation = model.getBaseLocation();
			}
		}
		finally
		{
			if (model != null)
				model.releaseFromRead();
		}
		if (baseLocation != null)
		{
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IPath filePath = new Path(baseLocation);
			IFile file = null;

			if (filePath.segmentCount() > 1)
			{
				file = root.getFile(filePath);
			}
			if (file != null)
			{
				IProject project = file.getProject();
				result = JavaCore.create(project);
			}
		}
		return result;
	}

}
