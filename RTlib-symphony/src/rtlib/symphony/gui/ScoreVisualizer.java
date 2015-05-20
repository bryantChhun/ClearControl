package rtlib.symphony.gui;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.tan;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import rtlib.core.variable.VariableListener;
import rtlib.core.variable.doublev.DoubleVariable;
import rtlib.core.variable.objectv.ObjectVariable;
import rtlib.symphony.movement.MovementInterface;
import rtlib.symphony.score.ScoreInterface;
import rtlib.symphony.staves.StaveInterface;
import rtlib.symphony.staves.ZeroStave;

public class ScoreVisualizer extends JPanel	implements
																						MouseMotionListener
{

	private static final long serialVersionUID = 1L;

	private final ObjectVariable<ScoreInterface> mScoreVariable;

	private final DoubleVariable mScalingVariable;

	@SuppressWarnings("unchecked")
	public ScoreVisualizer()
	{
		super();

		final VariableListener<?> lVariableListener = new VariableListener<Object>()
		{

			@Override
			public void setEvent(Object pCurrentValue, Object pNewValue)
			{
				SwingUtilities.invokeLater(() -> {
					repaint();
				});
			}

			@Override
			public void getEvent(Object pCurrentValue)
			{
				// TODO Auto-generated method stub

			}
		};

		mScalingVariable = new DoubleVariable("ScalingVariable", 1);
		mScalingVariable.addListener((VariableListener<Double>) lVariableListener);

		mScoreVariable = new ObjectVariable<ScoreInterface>("ScoreVariable");
		mScoreVariable.addListener((VariableListener<ScoreInterface>) lVariableListener);

		addMouseMotionListener(this);

	}

	public DoubleVariable getScalingVariable()
	{
		return mScalingVariable;
	}

	public ObjectVariable<ScoreInterface> getScoreVariable()
	{
		return mScoreVariable;
	}

	@Override
	public void paint(Graphics g)
	{
		final Graphics2D lGraphics2D = (Graphics2D) g;

		final int lWidth = getWidth();
		final int lHeight = getHeight();

		lGraphics2D.setColor(Color.black);
		lGraphics2D.fillRect(0, 0, lWidth, lHeight);

		final ScoreInterface lScore = getScoreVariable().get();

		if (lScore == null)
			return;

		// System.out.println(lScore.getTotalNumberOfTimePoints());
		if (lScore.getNumberOfMovements() == 0 || lScore.getDuration(TimeUnit.NANOSECONDS) == 0)
			return;

		final float lScaling = (float) mScalingVariable.getValue();
		final int lNumberOfMovements = lScore.getNumberOfMovements();
		final int lMaxNumberOfStaves = lScore.getMaxNumberOfStaves();
		final double lPixelsPerStave = ((double) lHeight) / lMaxNumberOfStaves;
		final long lTotalDuration = lScore.getDuration(TimeUnit.NANOSECONDS);
		// System.out.println("lMaxNumberOfStaves=" + lMaxNumberOfStaves);
		// System.out.println("lPixelsPerTimePoint=" + lPixelsPerTimePoint);
		// System.out.println("lPixelsPerStave=" + lPixelsPerStave);

		int lLastX = 0, lLastY = 0;

		double lMovementPixelOffset = 0;
		for (int m = 0; m < lNumberOfMovements; m++)
		{
			final MovementInterface lMovement = lScore.getMovement(m);
			final double lMovementWidthInPixels = (((lWidth) * lMovement.getDuration(TimeUnit.NANOSECONDS)) / lTotalDuration);

			for (int s = 0; s < lMovement.getNumberOfStaves(); s++)
			{
				final StaveInterface lStave = lMovement.getStave(s);

				if (!(lStave instanceof ZeroStave))
				{
					lLastX = round(lMovementPixelOffset);
					lLastY = round(lPixelsPerStave * s);
					for (int i = 0; i < lMovementWidthInPixels; i++)
					{
						final float lNormalizedTime = (float) ((i) / lMovementWidthInPixels);
						final float lFloatValue = lStave.getValue(lNormalizedTime);

						final float lBrightness = absclampplus(	lScaling * lFloatValue,
																										0.2f);
						final float lHue = 0.25f + (lFloatValue > 0f ? 0.5f : 0f);

						final float red = lBrightness * (lFloatValue <= 0f ? 1
																															: 0);
						final float green = lBrightness * 0.1f;
						final float blue = lBrightness * (lFloatValue >= 0f	? 1
																																: 0);

						lGraphics2D.setColor(Color.getHSBColor(	lHue,
																										0.5f,
																										lBrightness));/**/
						lGraphics2D.fillRect(	round(lMovementPixelOffset + i),
																	round(lPixelsPerStave * s),
																	roundmin1(1),
																	roundmin1(lPixelsPerStave));/**/

						final int lNewX = round(lMovementPixelOffset + i);
						final int lNewY = round(lPixelsPerStave * (s + 1)
																		- (clamp((1 + lScaling * lFloatValue) * 0.5f) * lPixelsPerStave));

						lGraphics2D.setColor(Color.white);
						lGraphics2D.drawLine(lLastX, lLastY, lNewX, lNewY);

						lLastX = lNewX;
						lLastY = lNewY;

					}
					lGraphics2D.setColor(Color.white);
					lGraphics2D.drawString(	lStave.getName(),
																	round(lMovementPixelOffset + 2),
																	12 + round(lPixelsPerStave * (s)));
				}



				lGraphics2D.setColor(Color.gray.darker());
				lGraphics2D.fillRect(	round(lMovementPixelOffset),
															round(lPixelsPerStave * s),
															round(lMovementWidthInPixels),
															1);

			}

			lGraphics2D.setColor(Color.white);
			lGraphics2D.drawLine(	round(lMovementPixelOffset),
														0,
														round(lMovementPixelOffset),
														lHeight);

			lMovementPixelOffset += lMovementWidthInPixels;
		}

	}

	private float absclampplus(float pX, float offset)
	{
		return min(1, max(0, abs(pX) + offset));
	}

	private float clamp(float pX)
	{
		return min(1, max(0, pX));
	}

	private static final int round(double pX)
	{
		return (int) Math.round(pX);
	}

	private static final int roundmin1(double pX)
	{
		return (int) max(1, Math.round(pX));
	}

	@Override
	public void mouseDragged(MouseEvent pE)
	{
		final float lX = pE.getX();
		final float lWidth = getWidth();
		final float lNormalizedX = lX / lWidth;
		final double lScale = tan(0.5 * PI * lNormalizedX);
		getScalingVariable().set(lScale);
		// System.out.println(lScale);
	}

	@Override
	public void mouseMoved(MouseEvent pE)
	{
		// TODO Auto-generated method stub

	}

}