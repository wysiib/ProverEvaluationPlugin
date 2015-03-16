package de.provereval.labelproviders;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eventb.core.IPOSequent;

public class SequentsLabelProvider implements ILabelProvider {

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public Image getImage(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element == null) {
			return "";
		}

		IPOSequent sequent = (IPOSequent) element;
		String projectName = sequent.getRodinProject().getElementName();
		String fileName = sequent.getRoot().getResource().getName()
				.replace(".bpo", "");

		return projectName + ":" + fileName + ":"
				+ sequent.getElementName().intern();

	}

}
