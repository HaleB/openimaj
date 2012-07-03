/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.hadoop.tools.downloader;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;
import org.openimaj.hadoop.tools.downloader.InputMode.Parser;
import org.openimaj.io.HttpUtils;
import org.openimaj.util.pair.IndependentPair;

/**
 * A Hadoop {@link Mapper} for downloading files.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class DownloadMapper extends Mapper<LongWritable, Text, Text, BytesWritable> {
	private static Logger logger = Logger.getLogger(DownloadMapper.class);
	
	private Parser parser;
	private long sleep;
	
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		HadoopDownloaderOptions options = new HadoopDownloaderOptions(context.getConfiguration().getStrings(HadoopDownloader.ARGS_KEY));
		options.prepare(false);
		
		parser = options.getInputParser();
		sleep = options.getSleep();
	}
	
	@Override
	public void map(LongWritable index, Text urlLine, Context context) {
		logger.info("Attempting to download: " + urlLine);
		
		try {
			IndependentPair<String, List<URL>> urlData = parser.parse(urlLine.toString());
			
			if (urlData == null) {
				logger.trace("parser returned null; record skipped.");
				return;
			}
			
			boolean downloaded = false;
			for (URL potential : urlData.secondObject()) {
				downloaded = tryDownload(urlData.firstObject(), potential, context);
				
				if (downloaded) {
					logger.info("Dowloaded: " + potential);
					return;
				}
				
				logger.trace("Not found; trying next");
			}
			
			if (!downloaded)
				logger.info("Failed to download: " + urlLine);
		} catch (Exception e) {
			logger.info("Error parsing: " + urlLine);
			logger.trace(e);
		}
		
		if (sleep > 0) {
			try {
				logger.trace("Waiting before continuing");
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				logger.trace("Wait was interupted; ignoring");
			}
		}
	}

	private boolean tryDownload(String key, URL url, Context context) throws InterruptedException {
		try {
			byte[] bytes = HttpUtils.readURLAsBytes(url);
			
			if (bytes == null)
				return false;
			
			BytesWritable bw = new BytesWritable(bytes);
			context.write(new Text(key), bw);
		} catch (IOException e) {
			logger.trace(e);
			return false;
		}
		
		return true;
	}
}
