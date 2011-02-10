package org.torquebox.base.deployers;

import java.io.File;
import java.io.IOException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileVisitor;
import org.jboss.vfs.VisitorAttributes;
import org.torquebox.base.metadata.RubyApplicationMetaData;

public class RubyApplicationExploder extends AbstractDeployer {

    public RubyApplicationExploder() {
        setStage( DeploymentStages.POST_PARSE );
        setInput( RubyApplicationMetaData.class );
        addOutput( RubyApplicationMetaData.class );
        setRelativeOrder( -1000 );
    }

    @Override
    public void deploy(DeploymentUnit unit) throws DeploymentException {
        RubyApplicationMetaData metaData = unit.getAttachment( RubyApplicationMetaData.class );
        VirtualFile root = metaData.getRoot();

        try {
            VirtualFile explodedRackRoot = getExplodedApplication( root );
            if (!root.equals( explodedRackRoot )) {
                metaData.explode( explodedRackRoot );
            }
        } catch (IOException e) {
            throw new DeploymentException( e );
        }
    }

    /**
     * This method is a hack to make sure the WAR is fully exploded. Currently
     * this is only needed for WARs that come through the DeclaredStructure
     * deployer. This should be removed when the DeclaredStructure deployer
     * correctly support exploding WARs.
     */
    private VirtualFile getExplodedApplication(VirtualFile virtualFile) throws IOException {
        if (virtualFile.isDirectory()) {
            VirtualFileVisitor visitor = new VirtualFileVisitor() {
                public void visit(VirtualFile vf) {
                    try {
                        File physicalFile = vf.getPhysicalFile();
                    } catch (IOException e) {
                        throw new RuntimeException( "Failed to force explosion of VirtualFile: " + vf, e );
                    }
                }

                public VisitorAttributes getAttributes() {
                    return VisitorAttributes.RECURSE_LEAVES_ONLY;
                }
            };
            virtualFile.visit( visitor );
        }

        File physicalRoot = virtualFile.getPhysicalFile();
        virtualFile = VFS.getChild( physicalRoot.getAbsolutePath() );

        return virtualFile;
    }

}