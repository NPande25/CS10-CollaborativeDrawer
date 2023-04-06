# Nikhil Pande
## CS10 23W - Problem Set 6
### Collaborative Drawing Interface
Problem Set 6 involves building a collaborative drawer, akin to Google Docs, where multiple clients/users can simultaneously access and edit the same canvas, with all edits visible to all users. This program uses client/server connections, with one master server and master canvas, which holds all the edits from each client. When one client connects to the server and makes an edit, it uses a specific messaging protocol to communicate its changes to the server's master canvas, which edits the canvas and broadcast these changes, through the same messaging protocol, to every other client. The drawer supports drawing lines, squares, ellipses, and other types of polygons.
