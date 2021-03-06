/*
 * generated by Xtext 2.22.0
 */
package edf.validation

import edf.forml0.AdditionExpression
import edf.forml0.BecomesExpression
import edf.forml0.BooleanFromCtl
import edf.forml0.BooleanLiteral
import edf.forml0.ChangesExpression
import edf.forml0.Clock
import edf.forml0.DivisionExpression
import edf.forml0.DropExpression
import edf.forml0.EventLiteral
import edf.forml0.EveryExpression
import edf.forml0.Expression
import edf.forml0.FirstExpression
import edf.forml0.FollowingExpression
import edf.forml0.FunctionCall
import edf.forml0.IfExpression
import edf.forml0.IntegerDivisionExpression
import edf.forml0.LeavesExpression
import edf.forml0.MyClock
import edf.forml0.MyRate
import edf.forml0.MyValue
import edf.forml0.NumericLiteral
import edf.forml0.PowerExpression
import edf.forml0.ProductExpression
import edf.forml0.PropertyEvent
import edf.forml0.PropertyPfd
import edf.forml0.PropertyState
import edf.forml0.Reference
import edf.forml0.SubstractionExpression
import edf.forml0.Time
import edf.forml0.UnaryMinusExpression
import edf.forml0.WhileExpression
import edf.forml0.WithoutExpression
import edf.forml0.inPTime
import edf.forml0.EqualityExpression
import edf.forml0.DifferenceExpression
import edf.forml0.LessThanExpression
import edf.forml0.LessOrEqualExpression
import edf.forml0.GreaterThanExpression
import edf.forml0.GreaterOrEqualExpression
import edf.forml0.NotExpression
import edf.forml0.AndExpression
import edf.forml0.OrExpression
import edf.forml0.XorExpression
import edf.forml0.BuiltInFunctionCall
import edf.forml0.AttributeExpression
import edf.forml0.Second
import edf.forml0.Tick
import edf.forml0.ClockTime
import edf.forml0.InPClockTime
import edf.forml0.MyDerivative
import edf.forml0.InIntervalExpression

//=============================================================================
//
//	Quantity of numeric expressions
//	Two quantities:
//	 -	time (real, in seconds, continuous time)
//	 -	ticks (integer, in number of clock ticks, discrete time)
//	Each quantity has an integer power (positive or negative), but At least one of them must be zero
//
//=============================================================================
class Forml0Quantity {
	public int time
	public int ticks
	
	new (int t1, int t2) {
		time  = t1
		ticks = t2
	}
	
	def boolean equal (Forml0Quantity q) {
		time == q.time && ticks == q.ticks
	}
	
	override String toString() {
		if      (time == 0 && ticks == 0) "scalar"
		else if (time == 1)				  "time"
		else if (ticks == 1)			  "tick"
		else if (time != 0)				  "time^" + time
		else							  "tick^" + ticks
	}
	
}

class Forml0QuantityProvider {
	public static val scalar   = new Forml0Quantity ( 0,  0)
	public static val time     = new Forml0Quantity ( 1,  0)
	public static val tick     = new Forml0Quantity ( 0,  1)
	public static val rate     = new Forml0Quantity (-1,  0)
	public static val tickRate = new Forml0Quantity ( 0, -1)
	
	def dispatch Forml0Quantity quantityFor (Expression expr) {
		switch expr {
			NumericLiteral:				scalar
			Second:						time
			Time:						time
			inPTime:					time
			Tick:						tick
			ClockTime:					tick
			InPClockTime:				tick
		///	MyDerivative:				
			MyRate:						rate
			PropertyPfd:				scalar
		///	BuiltInFunctionCall:		
			
			BooleanLiteral:				scalar
			PropertyState:				scalar
			BooleanFromCtl:				scalar
			
			MyValue:					scalar
			
			EventLiteral:				scalar
			PropertyEvent:				scalar
			MyClock:					scalar
			Clock:						scalar
			Reference:					scalar
			FunctionCall:				scalar
		
		///	AttributeExpression:		 
		///	PowerExpression:			
			UnaryMinusExpression:		expr.right ?. quantityFor ?: scalar
			NotExpression:				scalar
			FirstExpression:			scalar
			DropExpression:				scalar
		///	ProductExpression:			
		///	DivisionExpression:			
		///	IntegerDivisionExpression:	
			AdditionExpression:			expr.left ?. quantityFor ?: scalar
			SubstractionExpression:		expr.left ?. quantityFor ?: scalar
			WithoutExpression:			scalar
			FollowingExpression:		scalar
			EqualityExpression:			scalar
			DifferenceExpression:		scalar
			LessThanExpression:			scalar
			LessOrEqualExpression:		scalar
			GreaterThanExpression:		scalar
			GreaterOrEqualExpression:	scalar
			InIntervalExpression:		scalar
			AndExpression:				scalar
			WhileExpression:			scalar
			OrExpression:				scalar			
			XorExpression:				scalar			
			IfExpression:				expr.then ?. quantityFor ?: scalar
			EveryExpression:			scalar
			ChangesExpression:			scalar
			BecomesExpression:			scalar
			LeavesExpression:			scalar

		}
	}

	def dispatch Forml0Quantity quantityFor (MyDerivative expr) {
		if (expr.derivative && !expr.ranked) return rate
		if (expr.derivative &&  expr.ranked) return new Forml0Quantity (-expr.rank, 0)
		if (expr.integral   && !expr.ranked) return time
		if (expr.integral   &&  expr.ranked) return new Forml0Quantity ( expr.rank, 0)
		scalar
	}

	def dispatch Forml0Quantity quantityFor (BuiltInFunctionCall expr) {
		 switch expr.function {
		 	case 'count': 			scalar
		 	case 'duration':    	time
		 	case 'clockDuration':   tick
		 	case 'inPCount': 		scalar
		 	case 'inPDuration':  	time
		 	case 'inPClockDuration':tick
		 	case 'inPMax':			expr.argument?.quantityFor
		 	case 'inPMin':			expr.argument?.quantityFor
		 	case 'inTMax':			expr.argument?.quantityFor
		 	case 'inTMin':			expr.argument?.quantityFor
		 	case 'probability': 	scalar
		 	default:				scalar
		 }
	}
	
	def dispatch Forml0Quantity quantityFor (AttributeExpression expr) {
		if (expr.rate) 						 return rate
		if (expr.previous) 					 return expr.atom.quantityFor
		if (expr.derivative && !expr.ranked) return rate
		if (expr.derivative &&  expr.ranked) return new Forml0Quantity (-expr.rank, 0)
		if (expr.integral   && !expr.ranked) return time
		if (expr.integral   &&  expr.ranked) return new Forml0Quantity ( expr.rank, 0)
		expr.atom.quantityFor
	}

	def dispatch Forml0Quantity quantityFor (PowerExpression expr) {
		var left = expr.left ?. quantityFor ?: scalar
		new Forml0Quantity (left.time * (expr.negative ? -expr.right : expr.right), left.ticks * (expr.negative ? -expr.right : expr.right))
	}

	def dispatch Forml0Quantity quantityFor (ProductExpression expr) {
		var left  = expr.left  ?. quantityFor ?: scalar
		var right = expr.right ?. quantityFor ?: scalar
		new Forml0Quantity (left.time + right.time, left.ticks + right.ticks)
	}

	def dispatch Forml0Quantity quantityFor (DivisionExpression expr) {
		var left  = expr.left  ?. quantityFor ?: scalar
		var right = expr.right ?. quantityFor ?: scalar
		new Forml0Quantity (left.time - right.time, left.ticks - right.ticks)
	}

	def dispatch Forml0Quantity quantityFor (IntegerDivisionExpression expr) {
		var left  = expr.left  ?. quantityFor ?: scalar
		var right = expr.right ?. quantityFor ?: scalar
		new Forml0Quantity (left.time - right.time, left.ticks - right.ticks)
	}
	
}