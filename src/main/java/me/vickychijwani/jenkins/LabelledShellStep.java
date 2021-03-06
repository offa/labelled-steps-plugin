/*
 * The MIT License
 *
 * Copyright (c) 2013-2014, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.vickychijwani.jenkins;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.durabletask.BourneShellScript;
import org.jenkinsci.plugins.durabletask.DurableTask;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.durable_task.DurableTaskStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Runs a Bourne shell script asynchronously on a slave.
 *
 * This is a lightly-modified version of the official ShellStep:
 * https://github.com/jenkinsci/workflow-durable-task-step-plugin/blob/master/src/main/java/org/jenkinsci/plugins/workflow/steps/durable_task/ShellStep.java
 */
public final class LabelledShellStep extends DurableTaskStep {

    private final String script;
    private String label;

    @DataBoundConstructor public LabelledShellStep(String script) {
        if (script==null)
            throw new IllegalArgumentException();
        this.script = script;
    }

    public String getScript() {
        return script;
    }

    public String getLabel() {
        return label;
    }

    @DataBoundSetter
    public void setLabel(String label) {
        this.label = label;
    }

    @Override protected DurableTask task() {
        return new BourneShellScript(script);
    }

    @Override public StepExecution start(StepContext context) throws Exception {
        String path = context.get(EnvVars.class).get("PATH");
        if (path != null && path.contains("$PATH")) {
            context.get(TaskListener.class).getLogger().println("Warning: JENKINS-41339 probably bogus PATH=" + path + "; perhaps you meant to use ‘PATH+EXTRA=/something/bin’?");
        }
        return super.start(context);
    }

    @Extension public static final class DescriptorImpl extends DurableTaskStepDescriptor {

        @Override public String getDisplayName() {
            return "Shell Script";
        }

        @Override public String getFunctionName() {
            return "labelledShell";
        }

        @CheckForNull
        @Override
        public String argumentsToString(@Nonnull Map<String, Object> namedArgs) {
            if (namedArgs.containsKey("label")) {
                return (String) namedArgs.get("label");
            }
            return null;
        }

    }

}
