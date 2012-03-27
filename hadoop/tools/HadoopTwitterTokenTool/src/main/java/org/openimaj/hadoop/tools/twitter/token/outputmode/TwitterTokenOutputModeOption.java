package org.openimaj.hadoop.tools.twitter.token.outputmode;


import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.correlation.CorrelationOutputMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.jacard.JacardIndexOutputMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.SparseCSVTokenOutputMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.stats.StatsOutputMode;

/**
 * A twitter tweet token counting mode
 * 
 * @author ss
 *
 */
public enum TwitterTokenOutputModeOption implements CmdLineOptionsProvider{
	
	/**
	 * outputs a CSV file  
	 */
	CSV {

		@Override
		public TwitterTokenOutputMode getOptions() {
			return new SparseCSVTokenOutputMode();
		}
		
	},
	/**
	 * outputs the jacard index at each time step, a measure for how similar the sets of words are between two timesteps
	 */
	JACARD_INDEX {

		@Override
		public TwitterTokenOutputMode getOptions() {
			return new JacardIndexOutputMode();
		}
		
	},
	/**
	 * 
	 */
	CORRELATION{

		@Override
		public TwitterTokenOutputMode getOptions() {
			return new CorrelationOutputMode();
		}
		
	},
	/**
	 * Output some statistics about the words
	 */
	WORD_STATS{

		@Override
		public TwitterTokenOutputMode getOptions() {
			return new StatsOutputMode();
		}
		
	}, 
	/**
	 * don't do anything
	 */
	NONE {
		@Override
		public TwitterTokenOutputMode getOptions() {
			return new TwitterTokenOutputMode(){

				@Override
				public void write(HadoopTwitterTokenToolOptions opts,TwitterTokenMode completedMode) throws Exception {
					// do nothing
				}
				
			};
		}
	};

	@Override
	public abstract TwitterTokenOutputMode getOptions();
}
