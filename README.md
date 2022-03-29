For SmithWaterman.java,

SmithWaterman algorithm is for local alignment sequencing. It generates a scoring 2D matrix with a linear gap model. 
The traceback works, although it may be cutting corners with the approach to identifying the start position in the matrix. 
The strategy for traceback is actually solved here by generating arrows for the path of alignment, while also generating scores. 
So two matrices are generated at once simply because each position in the matrix holds an object and each object carries two data types. 
Let's say for example the last element in the 2D matrix is the starting point or the maximum element. You will always check the up left diagonal 
element for the direction to go next.  That is the key in this traceback. 
