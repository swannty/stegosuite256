package org.stegosuite.ui.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.stegosuite.image.format.ImageFormat;
import org.stegosuite.model.exception.SteganoImageException;

import java.util.ResourceBundle;

/**
 * Base class for the GUI. Contains global GUI-elements and global listeners.
 */
public class Gui {

	private final Display display;
	private final Shell shell;
	private final GuiComponents guiComponents;
	private Composite composite;
	private static Label statusBar;
	private final ResourceBundle L = ResourceBundle.getBundle("Messages");

	private int borderAlpha = 50;
	private String imagePath = null;

	public Gui(String path) {
		
		display = new Display();
		guiComponents = new GuiComponents();
		shell = guiComponents.createShell(display);
		Menu menuBar = guiComponents.createMenuBar(shell);

		if (path != null) {
			imagePath = path;
		} else {
			shell.addListener(SWT.Paint, event -> drawBorder(event, borderAlpha));
			showStartScreen();
		}

		// Drag and drop files into the window to load them
		final DropTarget dropTarget = new DropTarget(shell, DND.DROP_MOVE);	
		dropTarget.setTransfer(FileTransfer.getInstance());
		dropTarget.addDropListener(new DropTargetAdapter() {

			@Override
			public void drop(final DropTargetEvent event) {
				final String[] filenames = (String[]) event.data;
				loadImages(filenames[0]);
			}
			
			public void dragEnter(final DropTargetEvent event) {
				borderAlpha = 250;
				shell.redraw();
			}
			
			public void dragLeave(final DropTargetEvent event) {
				borderAlpha = 50;
				shell.redraw();
			}
		});

		// when user clicks in menubar on "Load file", open a file dialog
		menuBar.getItem(0).getMenu().getItem(0).addListener(SWT.Selection, event -> openFileDialog());
		shell.setMenuBar(menuBar);
		startEventLoop();
	}

	private void openFileDialog() {
		final String[] FILTER_NAMES = { "All supported files (*.bmp/*.gif/*.jpg/*.png)", "BMP-Files (*.bmp)",
				"GIF-Files (*.gif)", "JPG-Files (*.jpg)", "PNG-Files (*.png)" };
		final String[] FILTER_EXTS = { "*.bmp;*.gif;*.jpg;*.png", "*.bmp", "*.gif", "*.jpg", "*.png" };
		FileDialog dlg = new FileDialog(shell, SWT.OPEN);
		dlg.setFilterNames(FILTER_NAMES);
		dlg.setFilterExtensions(FILTER_EXTS);
		loadImages(dlg.open());
	}

	private void startEventLoop() {
		// Display Window in the middle of screen
		final Rectangle bds = display.getBounds();
		final Point p = shell.getSize();
		final int nLeft = (bds.width - p.x) / 2;
		final int nTop = (bds.height - p.y) / 2;
		shell.setBounds(nLeft, nTop, p.x, p.y);
		// ======================================
		shell.open();

		if (imagePath != null) {
			loadImages(imagePath);
		}

		// main loop
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	private void drawBorder(Event event, int alpha) {
		GC gc = event.gc;
		// gc.setForeground(event.display.getSystemColor(SWT.COLOR_GRAY));
		gc.setAntialias(SWT.ON);
		gc.setAlpha(alpha);

		gc.setLineWidth(2);
		gc.setLineStyle(SWT.LINE_CUSTOM);
		gc.setLineDash(new int[] { 10, 5 });

		gc.drawRoundRectangle(12, 12, shell.getClientArea().width - 24, shell.getClientArea().height - 24, 12, 12);
		gc.dispose();
	}

	/**
	 * Loads a gif- or bmp-image and displays it.
	 *
	 * @param path absolute file-path of the image
	 */
	private void loadImages(String path) {
		try {
			ImageFormat image = ImageFormat.getImageFormat(path);
			if (image != null) {
				initializeEmbedUi();
				guiComponents.embedUi.loadImage(image);
			}
		} catch (SteganoImageException e) {
			e.printStackTrace();
		}
	}

	private void initializeEmbedUi() {
		if (composite == null) {
			removeStartScreen();
			startLayout();
		}
	}

	private void removeStartScreen() {
		if (shell.getChildren().length >= 1) {
			shell.getChildren()[0].dispose();
			shell.removeListener(SWT.Paint, shell.getListeners(SWT.Paint)[0]);	
		}
	}

	/**
	 * Sets the message of the global status bar.
	 *
	 * @param s String which gets displayed.
	 */
	static void setStatusBarMsg(final String s) {
		statusBar.setText(s);
		statusBar.setToolTipText("");
	}

	static void setStatusBarMsg(final String statusbarText, final String tooltip) {
		statusBar.setText(statusbarText);
		statusBar.setToolTipText(tooltip);
	}

	private void showStartScreen() {
		final Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new FormLayout());
		final Label label = new Label(composite, SWT.SHADOW_NONE);
		final Label label2 = new Label(composite, SWT.SHADOW_NONE);
		final Button browseButton = new Button(composite, SWT.PUSH);

		label.setText(L.getString("start_text"));
		label2.setText(L.getString("or"));
		browseButton.setText(L.getString("browse_files"));

		// increase font size
		final FontData[] fontData = label.getFont().getFontData();
		for (FontData element : fontData) {
			element.setHeight(18);
			element.setStyle(SWT.BOLD);
		}
		label.setFont(new Font(display, fontData));

		// place label in the middle of the window
		shell.layout(true, true);

		FormData formData = new FormData();
		int offset = label2.getBounds().width / 2;
		formData.left = new FormAttachment(50, -offset);
		formData.top = new FormAttachment(label, 24);
		label2.setLayoutData(formData);

		formData = new FormData();
		offset = browseButton.getBounds().width / 2;
		formData.left = new FormAttachment(50, -offset);
		formData.top = new FormAttachment(label2, 24);
		browseButton.setLayoutData(formData);
		browseButton.addListener(SWT.Selection, event -> openFileDialog());
		shell.layout(true, true);

		formData = new FormData();
		offset = composite.getBounds().width / 2;
		formData.left = new FormAttachment(50, -offset);
		offset = composite.getBounds().height / 2;
		formData.top = new FormAttachment(50, -offset);

		composite.setLayoutData(formData);

		shell.layout(true, true);
	}

	private void startLayout() {
		statusBar = guiComponents.createStatusBar(shell);
		composite = guiComponents.createLayout(shell, statusBar);
		shell.layout(true, true);
	}
}
