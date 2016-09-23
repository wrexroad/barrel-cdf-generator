"use strict";
var J2000 = +(new Date("2000-01-01T11:58:55.816Z"));
var fs = require("fs");
var inFile = process.argv[2];
var outFile = process.argv[3] || inFile + ".out";

fs.readFile(inFile, "utf8", function(err, data) {
  let coords = [];
  if (err) {
    console.log(err);
    return;
  }
  
  data.split("\n").forEach((line) => {
    let values, date, alt, lat, lon;
    
    //skip blank lines
    line = line.trim();
    if (!line.length) {return;}

    //date is separated from the rest of the csv line with a ;
    values = line.split(";");
    date = values[0];
    //reformat the date as ns since J2000
    date = dateToJ2000(date);

    //break down the rest of the csv values
    values = values[1].split(",");
    
    //convert lat from DDmm.mm to DD.DD
    lat = +values[6].substr(0, 2);
    lat += (+values[6].substr(2)/60);
    
    //lat direction must be N
    if (values[7] !== "N") {
      lat *= -1;
    }

    //convert lon from DDmm.mm to DD.DD
    lon = +values[8].substr(0, 2);
    lon += (+values[8].substr(2)) / 60;
    
    //lon direction must be E 
    if (values[9] !== "E") {
      lon *= -1;
    }

    //convert alt from m to km
    alt = (+values[10]) / 1000;

    //add zeros to date to convert it to nanoseconds
    coords.push(date + "000000" + "," + lat + "," + lon + "," + alt);
  }, this);

  fs.writeFile(outFile, coords.join("\n"));
});

function dateToJ2000(date) {
  if (typeof date != "string") {
    return 0;
  }

  //format the date string into a UTC time
  date = date.replace(/ /, "T");
  date = date + "Z";

  //convert to a js object
  date = new Date(date);
  
  return ((+date) - J2000); 
}