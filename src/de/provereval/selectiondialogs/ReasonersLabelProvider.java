package de.provereval.selectiondialogs;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eventb.core.seqprover.ITactic;

public class ReasonersLabelProvider implements ILabelProvider {

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
		ITactic reasoner = (ITactic) element;
		return reasoner.toString();
	}

}