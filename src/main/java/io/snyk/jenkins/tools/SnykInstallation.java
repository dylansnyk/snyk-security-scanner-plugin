package io.snyk.jenkins.tools;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolInstaller;
import hudson.tools.ToolProperty;
import io.snyk.jenkins.tools.SnykBuildWrapper.SnykBuildWrapperDescriptor;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class SnykInstallation extends ToolInstallation implements EnvironmentSpecific<SnykInstallation>, NodeSpecific<SnykInstallation> {

  private transient Platform platform;

  @DataBoundConstructor
  public SnykInstallation(@Nonnull String name, @Nonnull String home, List<? extends ToolProperty<?>> properties) {
    this(name, home, properties, null);
  }

  private SnykInstallation(@Nonnull String name, @Nonnull String home, List<? extends ToolProperty<?>> properties, Platform platform) {
    super(name, home, properties);
    this.platform = platform;
  }

  @Override
  public SnykInstallation forEnvironment(EnvVars environment) {
    return new SnykInstallation(getName(), environment.expand(getHome()), getProperties().toList());
  }

  @Override
  public SnykInstallation forNode(@Nonnull Node node, TaskListener log) throws IOException, InterruptedException {
    return new SnykInstallation(getName(), translateFor(node, log), getProperties().toList());
  }

  // private Platform getPlatform() throws ToolDetectionException {
  //   Platform currentPlatform = platform;
  //
  //   if (currentPlatform == null) {
  //     Computer computer = Computer.currentComputer();
  //     if (computer != null) {
  //       Node node = computer.getNode();
  //       if (node == null) {
  //         throw new ToolDetectionException(Messages.Tools_nodeOffline());
  //       }
  //       currentPlatform = Platform.of(node);
  //     } else {
  //       // pipeline or master-to-slave case
  //       currentPlatform = Platform.current();
  //     }
  //     platform = currentPlatform;
  //   }
  //
  //   return currentPlatform;
  // }

  @Extension
  @Symbol("snyk")
  public static class SnykToolDescriptor extends ToolDescriptor<SnykInstallation> {

    @Nonnull
    @Override
    public String getDisplayName() {
      return "Snyk";
    }

    public List<? extends ToolInstaller> getDefaultInstallers() {
      return Collections.singletonList(new SnykInstaller(null));
    }

    @Override
    public SnykInstallation[] getInstallations() {
      Jenkins instance = Jenkins.getInstanceOrNull();
      if (instance == null) {
        throw new IllegalStateException("Jenkins has not been started, or was already shut down");
      }
      return instance.getDescriptorByType(SnykBuildWrapperDescriptor.class).getInstallations();
    }

    @Override
    public void setInstallations(SnykInstallation... installations) {
      Jenkins instance = Jenkins.getInstanceOrNull();
      if (instance == null) {
        throw new IllegalStateException("Jenkins has not been started, or was already shut down");
      }
      instance.getDescriptorByType(SnykBuildWrapperDescriptor.class).setInstallations(installations);
    }
  }
}
