package paragon.minecraft.library.client.ui.elements;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import paragon.minecraft.library.client.ClientUtilities;

public abstract class TextBox extends AbstractPlaceableGuiElement {

	/* Internal Fields */
	protected final Font FONT;
	protected Optional<FormattedText> TEXT = Optional.empty();

	public TextBox(@Nonnull final Font font) {
		this(font, null);
	}

	public TextBox(@Nonnull final Font font, @Nullable IPlacementMethod placement) {
		super(placement);
		this.FONT = Objects.requireNonNull(font, "Cannot render TextBox with null font!");
	}

	public void setDimensionsWithMargin(final int maxWidth, final int maxHeight, final int margin) {
		final int width = ClientUtilities.UI.getSizeWithMargin(maxWidth, margin);
		final int height = ClientUtilities.UI.getSizeWithMargin(maxHeight, margin);
		this.setDimensions(width, height);
	}

	public boolean hasText() {
		return this.TEXT.isPresent() && (this.TEXT.get().getString().length() > 0);
	}

	public void clearText() {
		this.setText(null);
	}
	
	public void updateWidthToFitText() {
		if (this.hasText()) {
			this.setWidth(this.FONT.width(this.TEXT.get()));
		}
	}

	public void setText(@Nullable FormattedText text) {
		this.TEXT = Optional.ofNullable(text);
	}

	@Override
	public void render(PoseStack pose, int cursorX, int cursorY, float delta) {
		if (this.canRender()) {
			this.doRender(pose, cursorX, cursorY, delta);
		}
	}

	/* Abstract Methods */

	protected abstract void doRender(PoseStack pose, int cursorX, int cursorY, float delta);

	/* Internal Methods */
	
	protected boolean sizeNonzero() {
		return (this.getWidth() > 0) && (this.getHeight() > 0);
	}
	
	protected boolean canRender() {
		return this.hasText() && this.sizeNonzero();
	}

	/* Variants */

	public static class Wrapped extends TextBox {

		public Wrapped(@Nonnull final Font font) {
			super(font);
		}

		public Wrapped(@Nonnull final Font font, @Nullable IPlacementMethod placement) {
			super(font, placement);
		}

		@Override
		public void doRender(PoseStack pose, int cursorX, int cursorY, float delta) {
			this.FONT.drawWordWrap(this.TEXT.get(), this.getX(), this.getY(), this.getWidth(), 0);
		}

	}

	public static class Paginated extends TextBox {

		/* Internal References */
		private List<FormattedCharSequence> lines = List.of();
		private int linesPerPage = 1;
		private int currentPage = 0;
		private int maxPage = 0;

		public Paginated(@Nonnull final Font font) {
			super(font);
		}

		public Paginated(@Nonnull final Font font, @Nullable IPlacementMethod placement) {
			super(font, placement);
		}

		/* Public Methods */

		public TextBox setPage(final int page) {
			this.currentPage = Mth.clamp(page, 0, this.maxPage);
			return this;
		}

		public TextBox changePage(final int value) {
			return this.setPage(this.currentPage + value);
		}

		public TextBox incrementPage() {
			return this.changePage(1);
		}

		public TextBox decrementPage() {
			return this.changePage(-1);
		}

		public int getPage() {
			return this.currentPage + 1;
		}

		public int getMaxPage() {
			return this.maxPage + 1;
		}

		public boolean hasNextPage() {
			return this.currentPage < this.maxPage;
		}

		public boolean hasPreviousPage() {
			return this.currentPage > 0;
		}

		/* Supertype Override Methods */

		@Override
		public void setText(@Nullable FormattedText text) {
			boolean differentText = !Objects.equals(this.TEXT.orElse(null), text);
			super.setText(text);
			if (differentText && this.hasText()) {
				this.updateTextWrapping();
			}
		}

		@Override
		public void setPlacementMethod(@Nullable IPlacementMethod method) {
			boolean needsUpdate = Objects.equals(method, this.getPlacementMethod().orElse(null));
			super.setPlacementMethod(method);
			if (needsUpdate) {
				this.updateTextWrapping();
			}
		}

		@Override
		public void setDimensions(final int width, final int height) {
			boolean needsUpdate = this.hasText() && ((width != this.getWidth()) || (height != this.getHeight()));
			this.width = width;
			this.height = height;
			if (needsUpdate) {
				this.updateTextWrapping();
			}
		}

		@Override
		public void setWidth(final int width) {
			boolean needsUpdate = this.needsUpdate(this.getWidth(), width);
			super.setWidth(width);
			if (needsUpdate) {
				this.updateTextWrapping();
			}
		}

		@Override
		public void setHeight(final int height) {
			boolean needsUpdate = this.needsUpdate(this.getHeight(), height);
			super.setHeight(height);
			if (needsUpdate) {
				this.updateTextWrapping();
			}
		}

		@Override
		public void doRender(PoseStack pose, int xPos, int yPos, float delta) {
			if (this.hasText() && this.sizeNonzero()) {
				final int minLine = this.currentPage * this.linesPerPage;
				final int lineOffset = this.FONT.lineHeight;
				for (int index = 0; this.hasLine(index + minLine) && (index < this.linesPerPage); index += 1) {
					this.drawLine(pose, index + minLine, this.getX(), this.getY() + (index * lineOffset));
				}
			}
		}

		/* Internal Methods */

		protected boolean needsUpdate(final int oldValue, final int newValue) {
			return this.hasText() && (oldValue != newValue);
		}

		protected void updateTextWrapping() {
			if (this.sizeNonzero()) {
				this.lines = this.FONT.split(this.TEXT.get(), this.getWidth());
				this.linesPerPage = (this.getHeight() / this.FONT.lineHeight);
				this.currentPage = 0; // TODO: Somehow set current page based on first visible lines before pagination switch?
				this.maxPage = Math.max(0, this.lines.size() / this.linesPerPage);
			}
		}

		protected void drawLine(PoseStack pose, int index, int xPos, int yPos) {
			this.FONT.draw(pose, this.lines.get(index), xPos, yPos, 0);
		}

		protected boolean hasLine(int index) {
			return (index >= 0) && (index < this.lines.size());
		}

	}

}