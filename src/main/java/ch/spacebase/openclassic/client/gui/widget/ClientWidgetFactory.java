package ch.spacebase.openclassic.client.gui.widget;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.model.SubTexture;
import ch.spacebase.openclassic.api.gui.Screen;
import ch.spacebase.openclassic.api.gui.widget.BlockPreview;
import ch.spacebase.openclassic.api.gui.widget.Button;
import ch.spacebase.openclassic.api.gui.widget.DefaultBackground;
import ch.spacebase.openclassic.api.gui.widget.FadingBox;
import ch.spacebase.openclassic.api.gui.widget.Image;
import ch.spacebase.openclassic.api.gui.widget.Label;
import ch.spacebase.openclassic.api.gui.widget.PasswordTextBox;
import ch.spacebase.openclassic.api.gui.widget.StateButton;
import ch.spacebase.openclassic.api.gui.widget.TextBox;
import ch.spacebase.openclassic.api.gui.widget.TranslucentBackground;
import ch.spacebase.openclassic.api.gui.widget.WidgetFactory;

public class ClientWidgetFactory extends WidgetFactory {

	@Override
	public BlockPreview newBlockPreview(int id, int x, int y, Screen parent, BlockType type) {
		return new ClientBlockPreview(id, x, y, parent, type);
	}

	@Override
	public BlockPreview newBlockPreview(int id, int x, int y, Screen parent, BlockType type, float scale) {
		return new ClientBlockPreview(id, x, y, parent, type, scale);
	}

	@Override
	public Button newButton(int id, int x, int y, Screen parent, String text) {
		return new ClientButton(id, x, y, parent, text);
	}

	@Override
	public Button newButton(int id, int x, int y, int width, int height, Screen parent, String text) {
		return new ClientButton(id, x, y, width, height, parent, text);
	}

	@Override
	public DefaultBackground newDefaultBackground(int id, Screen parent) {
		return new ClientDefaultBackground(id, parent);
	}

	@Override
	public FadingBox newFadingBox(int id, int x, int y, int width, int height, Screen parent, int color, int fadeTo) {
		return new ClientFadingBox(id, x, y, width, height, parent, color, fadeTo);
	}

	@Override
	public Image newImage(int id, int x, int y, Screen parent, SubTexture tex) {
		return new ClientImage(id, x, y, parent, tex);
	}

	@Override
	public Label newLabel(int id, int x, int y, Screen parent, String text) {
		return new ClientLabel(id, x, y, parent, text);
	}

	@Override
	public Label newLabel(int id, int x, int y, Screen parent, String text, boolean xCenter) {
		return new ClientLabel(id, x, y, parent, text, xCenter);
	}

	@Override
	public Label newLabel(int id, int x, int y, Screen parent, String text, boolean xCenter, boolean scaled) {
		return new ClientLabel(id, x, y, parent, text, xCenter, scaled);
	}

	@Override
	public PasswordTextBox newPasswordTextBox(int id, int x, int y, Screen parent) {
		return new ClientPasswordTextBox(id, x, y, parent);
	}

	@Override
	public PasswordTextBox newPasswordTextBox(int id, int x, int y, Screen parent, int max) {
		return new ClientPasswordTextBox(id, x, y, parent, max);
	}

	@Override
	public PasswordTextBox newPasswordTextBox(int id, int x, int y, int width, int height, Screen parent) {
		return new ClientPasswordTextBox(id, x, y, width, height, parent);
	}

	@Override
	public PasswordTextBox newPasswordTextBox(int id, int x, int y, int width, int height, Screen parent, int max) {
		return new ClientPasswordTextBox(id, x, y, width, height, parent, max);
	}

	@Override
	public PasswordTextBox newPasswordTextBox(int id, int x, int y, Screen parent, boolean chatbox) {
		return new ClientPasswordTextBox(id, x, y, parent, chatbox);
	}

	@Override
	public PasswordTextBox newPasswordTextBox(int id, int x, int y, Screen parent, int max, boolean chatbox) {
		return new ClientPasswordTextBox(id, x, y, parent, max, chatbox);
	}

	@Override
	public PasswordTextBox newPasswordTextBox(int id, int x, int y, int width, int height, Screen parent, boolean chatbox) {
		return new ClientPasswordTextBox(id, x, y, width, height, parent, chatbox);
	}

	@Override
	public PasswordTextBox newPasswordTextBox(int id, int x, int y, int width, int height, Screen parent, int max, boolean chatbox) {
		return new ClientPasswordTextBox(id, x, y, width, height, parent, max, chatbox);
	}

	@Override
	public StateButton newStateButton(int id, int x, int y, Screen parent, String text) {
		return new ClientStateButton(id, x, y, parent, text);
	}

	@Override
	public StateButton newStateButton(int id, int x, int y, int width, int height, Screen parent, String text) {
		return new ClientStateButton(id, x, y, width, height, parent, text);
	}

	@Override
	public TextBox newTextBox(int id, int x, int y, Screen parent) {
		return new ClientTextBox(id, x, y, parent);
	}

	@Override
	public TextBox newTextBox(int id, int x, int y, Screen parent, int max) {
		return new ClientTextBox(id, x, y, parent, max);
	}

	@Override
	public TextBox newTextBox(int id, int x, int y, int width, int height, Screen parent) {
		return new ClientTextBox(id, x, y, width, height, parent);
	}

	@Override
	public TextBox newTextBox(int id, int x, int y, int width, int height, Screen parent, int max) {
		return new ClientTextBox(id, x, y, width, height, parent, max);
	}

	@Override
	public TextBox newTextBox(int id, int x, int y, Screen parent, boolean chatbox) {
		return new ClientTextBox(id, x, y, parent, chatbox);
	}

	@Override
	public TextBox newTextBox(int id, int x, int y, Screen parent, int max, boolean chatbox) {
		return new ClientTextBox(id, x, y, parent, max, chatbox);
	}

	@Override
	public TextBox newTextBox(int id, int x, int y, int width, int height, Screen parent, boolean chatbox) {
		return new ClientTextBox(id, x, y, width, height, parent, chatbox);
	}

	@Override
	public TextBox newTextBox(int id, int x, int y, int width, int height, Screen parent, int max, boolean chatbox) {
		return new ClientTextBox(id, x, y, width, height, parent, max, chatbox);
	}

	@Override
	public TranslucentBackground newTranslucentBackground(int id, Screen parent) {
		return new ClientTranslucentBackground(id, parent);
	}

}
