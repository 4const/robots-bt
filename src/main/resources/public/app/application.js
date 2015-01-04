$(function() {
	__pointRadius = 7;
	__lineLenght = 100;

	__transportersColors = ['#FF0000', '#0000FF', '#FF8000']
	
	__readSystemState = function(cb) {
		$.ajax({
			type: 'GET',
			url: '/state',
			async: false,
			success: cb
		});
	};
	
	__revertDirection = function(direction) {
		switch (direction) {
			case 'Top': return 'Bottom';
			case 'Right': return 'Left';
			case 'Bottom': return 'Top';
			case 'Left': return 'Right';
		}
	}
	
	__extractActions = function(transportersState) {
		var result = [];
		
		for (i in transportersState) {
			var task = transportersState[i].currentTask;
			
			var stay = null;
			var move = null;
			if (!!task.in) {
				stay = { in: task.in };
			} else {
				move = { from: task.from, to: task.to };
			}
			
			result.push({
				id: i,
				color: __transportersColors.pop(),
				stay: stay,
				move: move,
				inCrossroad: function(crossroad) { 
					return !!this.stay && this.stay.in == crossroad; 
				},
				isMoving: function(f, t) { 
					return !!this.move && this.move.from == f && this.move.to == t
				}
			});
		}
		
		return result;
	};
	
	__getPoint = function(source, link, crossroads) {
		var getDelta = function(direction) {
			var deltaX = 0;
			var deltaY = 0;
			switch (direction) {
				case 'Top': deltaY = -1; break;
				case 'Right': deltaX = 1; break;
				case 'Bottom': deltaY = 1; break;
				case 'Left': deltaX = -1; break;
			}
			return { x: deltaX, y: deltaY };
		}
		var direction = link.direction; 
		var delta = getDelta(direction);
		
		var toCrossroadLinks = crossroads[link.to].links;
		var otherDirection;
		for (k in toCrossroadLinks) {
			var l = toCrossroadLinks[k]
			if (l.to == link.from) {
				otherDirection = l.direction
			}
		}
		
		otherDirection = __revertDirection(otherDirection);
		if (direction != otherDirection) {
			var otherDelta = getDelta(otherDirection);
			delta.x += otherDelta.x
			delta.y += otherDelta.y
		}
		
		return { x: source.x + delta.x, y: source.y + delta.y }
	};
	
	__drawLegend = function(transportersAction, mapSvg) {
		var x = 50;
		var y = 50;
		transportersAction.forEach(function(t) {
			mapSvg.circle(x, y, 10, { fill: t.color });
			mapSvg.text(x + 30, y + 5, '#' + t.id,  
				{ fontFamily: 'Verdana', fontSize: 18, textAnchor: 'middle' }); 				
			y += 25; 
		});
	};	
	
	__drawPoint = function(point, color, startPoint, mapSvg) {	 
		mapSvg.circle( 
			startPoint.x + point.x * __lineLenght, 
			startPoint.y + point.y * __lineLenght, 
			__pointRadius, 
			{ stroke: '#000', fill: color }); 
	};
	
	__drawLine = function(from, to, color, startPoint, mapSvg) {	 
		var path = mapSvg.createPath(); 
		var radius = __lineLenght;
		mapSvg.path( 
			path.move(startPoint.x + from.x * __lineLenght, 
					startPoint.y + from.y * __lineLenght)
				.arc(radius, radius, 0, 0, 0, 
					startPoint.x + to.x * __lineLenght, 
					startPoint.y + to.y * __lineLenght),
			{ stroke: color, strokeWidth: '2px', fill: 'none' }); 
	};

	
	$('#map').svg({ 
		onLoad: function drawInitial(svg) { } 
	});	
    __readSystemState(function(res) {
		var transportersAction = __extractActions(res.transportersState);
	
    	var crossroads = res.transportMap.crossroads;
		
		var minX = 0; 
		var minY = 0;
		var maxY = 0;
		var points = []; points[1] = { x: 0, y:0 };
		for (i in crossroads) {
			var c = crossroads[i];
			var source = points[i];
			
			for (j in c.links) {
				var link = c.links[j]
				
				if (!points[link.to]) {
					points[link.to] = __getPoint(source, link, crossroads); 
				}
				var point = points[link.to]; 
				
				minX = Math.min(minX, point.x);
				minY = Math.min(minY, point.y);
				maxY = Math.max(maxY, point.y);
			}
		}
		
		var startPoint = {
			x: Math.abs(minX * __lineLenght) + __lineLenght,
			y: Math.abs(minY * __lineLenght) + __lineLenght
		}
		
		var mapSvg = $('#map').svg('get');

		var drawnLines = [];
		for (i in crossroads) {
			var c = crossroads[i];
			var source = points[i];
			
			var pointColor = '#000';
			transportersAction.forEach(function(t) {
				if (t.inCrossroad(i)) {
					pointColor = t.color;
				}
			});
			__drawPoint(source, pointColor, startPoint, mapSvg);			

			for (j in c.links) {
				var link = c.links[j]
				if (!link.open) { continue; }
				
				var lineColor = '#000';
				transportersAction.forEach(function(t) {
					if (t.isMoving(i, link.to)) {
						lineColor = t.color;
					}
				});				
				__drawLine(points[link.from], points[link.to], lineColor, startPoint, mapSvg);
			}
		}
		__drawLegend(transportersAction, mapSvg);
		$('#map').height((maxY - minY + 2) * __lineLenght);
		$('#map svg').height((maxY - minY + 2) * __lineLenght);
		
		$('#state').text(JSON.stringify(res));
    });
});