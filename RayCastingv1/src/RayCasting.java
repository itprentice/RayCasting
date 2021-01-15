import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.opengl.GL11.*;

public class RayCasting {
    /**
     * Creates a window using GLFW
     * @param title : The title of the window
     * @param width : The width (in pixels) of the window
     * @param height : The height (in pixels) of the window
     * @param resizable : True or False
     */
    public static long setWindow(String title, int width, int height,  boolean resizable){
        GLFWErrorCallback errorCallback;
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints(); // Loads GLFW's default window settings
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE); // Sets window to be visible
        glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE); // Sets whether the window is resizable
        long id = glfwCreateWindow(width, height, title, NULL, NULL); // Does the actual window creation, NULL = 0Ls
        if ( id == NULL ) throw new RuntimeException("Failed to create window");

        glfwMakeContextCurrent(id); // glfwSwapInterval needs a context on the calling thread, otherwise will cause NO_CURRENT_CONTEXT error
        GL.createCapabilities(); // Will let lwjgl know we want to use this context as the context to draw with

        glfwSwapInterval(1); // How many draws to swap the buffer
        glfwShowWindow(id); // Shows the window

        return id;
    }

    public static void drawRect(int x, int y, int dx, int dy){
        glBegin(GL_QUADS);
            glVertex2i(x, y);
            glVertex2i(x+dx, y);
            glVertex2i(x+dx, y+dy);
            glVertex2i(x, y+dy);
        glEnd();
    }

    public static void drawHTLine(int x, int y, int dx, int dy){
        glBegin(GL_LINES);
            glVertex2i(x, y);
            glVertex2i(x+dx, y+dy);
        glEnd();
    }

    public static void draw2dSquareMap(int[] objectMatrix, int sLength, int squaresPerRow, byte red, byte green, byte blue){
        int xCoord = 0, yCoord = 0;
        for (int i = 0; i < objectMatrix.length; i++){
            if (objectMatrix[i] == 1){
                glColor3ub(red, green, blue);
                drawRect(xCoord, yCoord, sLength, sLength);
            }
            glColor3ub((byte)133, (byte)133, (byte)133);
            drawHTLine(xCoord, yCoord, sLength, 0);
            drawHTLine(xCoord, yCoord, 0, sLength);
            xCoord += sLength;
            if ((i+1)%squaresPerRow == 0){
                xCoord = 0;
                yCoord += sLength;
            }
        }
    }
    /*public static void draw3dRays(int[][] matrix, int x, int y, int size, double ang, double zAng,  int fov, int rectWidth, int blocksPerRow, int blocksPerCol){
        int numOfRects=(blocksPerRow*size)/rectWidth;
        double deg = Math.PI/((numOfRects/(double)fov)*180.0);
        for (int i = 0; i < numOfRects; i++){
            double theta = ang +((i-numOfRects/2.0)*deg);
            if (theta < 0) theta += 2*Math.PI;
            else if (theta > 2*Math.PI) theta -= 2*Math.PI;

            double tan = Math.tan(theta), rTan = 1.0/tan, hLength = 1000000, vLength = 1000000, bLength = 1000000;
            int  index = 0, dof = 8;
            double dx = 0, dy = 0, hy = y, hx = x, vx = x, vy = y;

            // Horizontal Line Check
            if (theta > Math.PI){
                int yo = y%size;
                dy = -size;
                dx = dy*rTan;
                hy = y-yo;
                hx = x - yo*rTan;
                dof = 0;
            }
            else if (theta < Math.PI){
                int yo = size - y%size;
                dy = size;
                dx = dy*rTan;
                hy = y+yo;
                hx = x+yo*rTan;
                dof = 0;
            }
            while (dof < 8){
                index = (int)(hy/size)*blocksPerRow+(int)(hx/size);
                if (0 < index && index < matrix.length && matrix[index] == 1) {
                    dof = 8;
                    hLength = Math.sqrt((x-hx)*(x-hx)+(y-hy)*(y-hy));
                }
                else if (0 < (index-blocksPerRow)  && (index-blocksPerRow) < matrix.length && matrix[index-blocksPerRow] == 1) {
                    dof = 8;
                    hLength = Math.sqrt((x-hx)*(x-hx)+(y-hy)*(y-hy));
                }
                else{
                    hx += dx;
                    hy += dy;
                    dof++;
                }
            }
            //Vertical Line Check
            dof = 8;
            if (Math.PI/2 < theta && theta < 3*Math.PI/2){
                int xo = x%size;
                dx = -size;
                dy = dx*tan;
                vx = x-xo;
                vy = y - xo*tan;
                dof = 0;
            }
            else if (theta > 3*Math.PI/2 || Math.PI/2 > theta){
                int xo = size - x%size;
                dx = size;
                dy = dx*tan;
                vx = x+xo;
                vy = y+xo*tan;
                dof = 0;
            }
            while (dof < 8){
                index = (int)(vy/size)*blocksPerRow+(int)(vx/size);
                if (0 < index && index < matrix.length && matrix[index] == 1) {
                    dof = 8;
                    vLength = Math.sqrt((x - vx)*(x - vx) + (y - vy)*(y - vy));

                }
                else if (0 < (index-1) && (index-1) < matrix.length && matrix[index-1] == 1) {
                    dof = 8;
                    vLength = Math.sqrt((x - vx)*(x - vx) + (y - vy)*(y - vy));

                }
                else{
                    vx += dx;
                    vy += dy;
                    dof++;
                }
            }

            if (hLength <= vLength){
                glColor3f(0, 1, 1);
                glBegin(GL_LINES);
                glVertex2i(x, y);
                glVertex2i((int)hx, (int)hy);
                glEnd();
                bLength = hLength;
            }
            else if (vLength < hLength){
                glColor3f(0, 0.7f, 0.7f);
                glBegin(GL_LINES);
                glVertex2i(x, y);
                glVertex2i((int)vx, (int)vy);
                glEnd();
                bLength = vLength;
            }
            double phi = ang - theta;
            if (phi < 0) phi += 2*Math.PI;
            if (phi > 2*Math.PI) phi -= 2*Math.PI;
            bLength = bLength*Math.cos(phi);
            int yRes = blocksPerCol*size, xRes = numOfRects*rectWidth;
            double lineHeight = (double)(size*yRes)/bLength;
            if (lineHeight > yRes) lineHeight = yRes;

            double lineOffset = yRes/2.0 - lineHeight/2.0;
            glLineWidth(rectWidth);
            glBegin(GL_LINES);
            glVertex2i(xRes+rectWidth/2+rectWidth*(i), (int)lineOffset);
            glVertex2i(xRes+rectWidth/2+rectWidth*(i), (int)(lineHeight +lineOffset));
            glEnd();
            glLineWidth(1);
        }
    }*/

    /**
     *
     * @param matrix 2d array where each row index is a level in terms of height (z-axis) and each inner array represents an xy map
     * @param x positional coordinate of the player
     * @param y positional coordinate of the player
     * @param z positional coordinate of the player
     * @param xyAng current xy angle that the player is looking at (uses flipped CAST rule)
     * @param xyzAng
     * @param xyFov
     * @param pixelSize
     * @param size
     * @param xBlocks
     * @param yBlocks
     * @param zBlocks
     */
    public static void draw3dRays(int[][] matrix, int horRes, int vertRes, int x, int y, int z, double xyAng, double xyzAng, int xyFov, int pixelSize, int size, int xBlocks, int yBlocks, int zBlocks){
        int numOfHorPixels = horRes/pixelSize, numOfVertPixels = vertRes/pixelSize;

    }
    /**
     *
     * @param matrix int matrix comprised of 0s and 1s that represents the layout of the xy map (1 = block is there, 0 = block is not there)
     * @param x x-cord of the player
     * @param y y-cord of the player
     * @param size side length of a block
     * @param ang current xy angle that the player is looking at (uses flipped cast)
     * @param fov the width of the field of view in degrees
     * @param rectWidth the width of each rectangle it draws to create the 3d effect
     * @param blocksPerRow the number of blocks in a column of the map
     * @param blocksPerCol the number of blocks in a row of the map
     */
    public static void draw2dRays(int[] matrix, int x, int y, int size, double ang,  int fov, int rectWidth, int blocksPerRow, int blocksPerCol){
        int numOfRects=(blocksPerRow*size)/rectWidth; // this only works because i made the display resolution the same for the minimap
        double deg = Math.PI/((numOfRects/(double)fov)*180.0);
        for (int i = 0; i < numOfRects; i++){
            double theta = ang +((i-numOfRects/2.0)*deg);
            if (theta < 0) theta += 2*Math.PI;
            else if (theta > 2*Math.PI) theta -= 2*Math.PI;

            double tan = Math.tan(theta), rTan = 1.0/tan, hLength = 1000000, vLength = 1000000, bLength = 1000000;
            int  index = 0, dof = 8;
            double dx = 0, dy = 0, hy = y, hx = x, vx = x, vy = y;

            // Horizontal Line Check
            if (theta > Math.PI){
                int yo = y%size;
                dy = -size;
                dx = dy*rTan;
                hy = y-yo;
                hx = x - yo*rTan;
                dof = 0;
            }
            else if (theta < Math.PI){
                int yo = size - y%size;
                dy = size;
                dx = dy*rTan;
                hy = y+yo;
                hx = x+yo*rTan;
                dof = 0;
            }
            while (dof < 8){
                index = (int)(hy/size)*blocksPerRow+(int)(hx/size);
                if (0 < index && index < matrix.length && matrix[index] == 1) {
                    dof = 8;
                    hLength = Math.sqrt((x-hx)*(x-hx)+(y-hy)*(y-hy));
                }
                else if (0 < (index-blocksPerRow)  && (index-blocksPerRow) < matrix.length && matrix[index-blocksPerRow] == 1) {
                    dof = 8;
                    hLength = Math.sqrt((x-hx)*(x-hx)+(y-hy)*(y-hy));
                }
                else{
                    hx += dx;
                    hy += dy;
                    dof++;
                }
            }
            //Vertical Line Check
            dof = 8;
            if (Math.PI/2 < theta && theta < 3*Math.PI/2){
                int xo = x%size;
                dx = -size;
                dy = dx*tan;
                vx = x-xo;
                vy = y - xo*tan;
                dof = 0;
            }
            else if (theta > 3*Math.PI/2 || Math.PI/2 > theta){
                int xo = size - x%size;
                dx = size;
                dy = dx*tan;
                vx = x+xo;
                vy = y+xo*tan;
                dof = 0;
            }
            while (dof < 8){
                index = (int)(vy/size)*blocksPerRow+(int)(vx/size);
                if (0 < index && index < matrix.length && matrix[index] == 1) {
                    dof = 8;
                    vLength = Math.sqrt((x - vx)*(x - vx) + (y - vy)*(y - vy));

                }
                else if (0 < (index-1) && (index-1) < matrix.length && matrix[index-1] == 1) {
                    dof = 8;
                    vLength = Math.sqrt((x - vx)*(x - vx) + (y - vy)*(y - vy));

                }
                else{
                    vx += dx;
                    vy += dy;
                    dof++;
                }
            }

            if (hLength <= vLength){
                glColor3f(0, 1, 1);
                glBegin(GL_LINES);
                glVertex2i(x, y);
                glVertex2i((int)hx, (int)hy);
                glEnd();
                bLength = hLength;
            }
            else if (vLength < hLength){
                glColor3f(0, 0.7f, 0.7f);
                glBegin(GL_LINES);
                glVertex2i(x, y);
                glVertex2i((int)vx, (int)vy);
                glEnd();
                bLength = vLength;
            }
            double phi = ang - theta;
            if (phi < 0) phi += 2*Math.PI;
            if (phi > 2*Math.PI) phi -= 2*Math.PI;
            bLength = bLength*Math.cos(phi);
            int yRes = blocksPerCol*size, xRes = numOfRects*rectWidth;
            double lineHeight = (double)(size*yRes)/bLength;
            if (lineHeight > yRes) lineHeight = yRes;

            double lineOffset = yRes/2.0 - lineHeight/2.0;
            glLineWidth(rectWidth);
            glBegin(GL_LINES);
                glVertex2i(xRes+rectWidth/2+rectWidth*(i), (int)lineOffset);
                glVertex2i(xRes+rectWidth/2+rectWidth*(i), (int)(lineHeight +lineOffset));
            glEnd();
            glLineWidth(1);
        }

    }

    public static void main (String[] args){
        int lilL = 8, lilW = 8, lilBH = 2,  lilX = 346, lilY = 346, lilZ, lilXP = 0, lilYP = -2, speed = 2,  wSize = 700, blockPerRow = 10, blockSize = 70;
        double lilA = 3*Math.PI/2.0;
        int[] layout = {
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 0, 0, 0, 1, 0, 0, 0, 0, 1,
                1, 0, 0, 0, 1, 0, 0, 1, 0, 1,
                1, 0, 1, 0, 1, 0, 0, 0, 0, 1,
                1, 0, 0, 0, 0, 0, 1, 0, 0, 1,
                1, 0, 1, 1, 0, 0, 0, 0, 0, 1,
                1, 0, 1, 0, 0, 0, 1, 1, 1, 1,
                1, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                1, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        int[] layoutTwo = {
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 0, 0, 0, 1, 0, 0, 0, 0, 1,
                1, 0, 0, 0, 1, 0, 0, 1, 0, 1,
                1, 0, 1, 0, 1, 0, 0, 0, 0, 1,
                1, 0, 0, 0, 0, 0, 1, 0, 0, 1,
                1, 0, 1, 1, 0, 0, 0, 0, 0, 1,
                1, 0, 1, 0, 0, 0, 1, 1, 1, 1,
                1, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                1, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        int[] layoutThree = {
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 0, 0, 0, 1, 0, 0, 0, 0, 1,
                1, 0, 0, 0, 1, 0, 0, 1, 0, 1,
                1, 0, 1, 0, 1, 0, 0, 0, 0, 1,
                1, 0, 0, 0, 0, 0, 1, 0, 0, 1,
                1, 0, 1, 1, 0, 0, 0, 0, 0, 1,
                1, 0, 1, 0, 0, 0, 1, 1, 1, 1,
                1, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                1, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        int[][] finalLayout = {layout, layoutTwo, layoutThree};
        long win = setWindow("bruh", wSize*2, wSize, false);
        //ensures that the coordinate system for drawing is left top corner bound is (0, 0) right bottom corner bound is (width, height)
        glViewport(0, 0, wSize*2, wSize);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, wSize*2, wSize, 0, -1, 1);


        while(!glfwWindowShouldClose(win)){
            glfwPollEvents();
                if (glfwGetKey(win, GLFW_KEY_E) == GLFW_TRUE){
                    lilA += 0.05;
                    if (lilA > 2*Math.PI) lilA -= 2*Math.PI;
                    lilXP = (int)Math.round(speed*Math.cos(lilA));
                    lilYP = (int)Math.round(speed*Math.sin(lilA));
                }
                else if (glfwGetKey(win, GLFW_KEY_Q) == GLFW_TRUE) {
                    lilA -= 0.05;
                    if (lilA < 0) lilA += 2*Math.PI;
                    lilXP = (int)Math.round(speed*Math.cos(lilA));
                    lilYP = (int)Math.round(speed*Math.sin(lilA));
                }

                if (glfwGetKey(win, GLFW_KEY_W) == GLFW_TRUE){
                    lilX += lilXP;
                    lilY += lilYP;
                }
                else if (glfwGetKey(win, GLFW_KEY_S) == GLFW_TRUE) {
                    lilX -= lilXP;
                    lilY -= lilYP;
                }
                if (glfwGetKey(win, GLFW_KEY_A) == GLFW_TRUE){
                    lilX += lilYP;
                    lilY -= lilXP;
                }
                else if (glfwGetKey(win, GLFW_KEY_D) == GLFW_TRUE) {
                    lilX -= lilYP;
                    lilY += lilXP;
                }
            glClear(GL_COLOR_BUFFER_BIT);
            draw2dSquareMap(layout, blockSize, blockPerRow, (byte)157, (byte)194, (byte)237);
            
            glColor3f(0, 1, 1);
            draw2dRays(layout,lilX+(lilW/2), lilY+(lilL/2), blockSize, lilA, 60, 1, 10, 10);

            glColor3f(1, 0, 1);
            drawRect(lilX, lilY, lilW, lilL); //draws lilGuy

            glColor3f(1, 1, 0);
            drawHTLine(lilX+(lilW/2), lilY+(lilL/2), 10*lilXP, 10*lilYP);


            glfwSwapBuffers(win);
        }
    }
}
